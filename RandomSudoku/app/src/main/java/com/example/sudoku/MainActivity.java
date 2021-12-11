package com.example.sudoku;

import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    TableLayout table;
    RadioGroup inputButtons;
    SudokuButtonListener listener;
    Switch memoSwitch;
    Button resumeBtn;
    Button undoBtn;
    Button pauseBtn;
    Button eraseAllBtn;
    TextView timerText;
    TextView difficultyText;
    SudokuButton[][] buttons;
    int[][] tempBoard;
    int[][] finalBoard;
    boolean[][] row;
    boolean[][] col;
    boolean[][] box;//   | 0 | 1 | 2 |
                    //   | 3 | 4 | 5 |
                    //   | 6 | 7 | 8 |
    final int memoTextSize = 25;
    final int defaultTextSize = 50;
    //배경색
    final int DEFAULT = 0;
    final int CHECK = 1;
    final int SAMENUM = 2;
    final int WARN = 3;
    //난이도
    public static final int[] VERYEASY = {34,35,36};
    public static final int[] EASY = {43,44,45};
    public static final int[] NORMAL = {47,48,49};
    public static final int[] HARD = {51,52,53};
    public static int difficulty=0;

    BoardStack boardStack;
    SudokuButton selected;
    int hintCount;

    long baseTime;  long pauseTime;
    long hour; long minute; long second;
    final Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(@NonNull Message message) {   //실제로 타이머가 할 일
            long overTime = SystemClock.elapsedRealtime() - baseTime;
            hour = overTime/1000/60/60;
            minute = (overTime/1000/60)%60;
            second = (overTime/1000)%60;
            timerText.setText(String.format("%02d:%02d:%02d", hour, minute, second));
            handler.sendEmptyMessage(0);
        }
    };

    @Override   //앱 시작
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        difficultyText = (TextView)findViewById(R.id.difficultyText);
        timerText = (TextView)findViewById(R.id.timerText);
        table = (TableLayout)findViewById(R.id.tableLayout);
        listener = new SudokuButtonListener();
        gameStart();
        setButtonsClickListener();
    }
    @Override   //메뉴
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }
    @Override   //메뉴 아이템
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if(resumeBtn.getVisibility()==View.VISIBLE) return false;
        int randN = (int)(Math.random()*3);
        switch (item.getItemId()){
            case R.id.veryEasy: difficulty = VERYEASY[randN];  break;
            case R.id.easy:     difficulty = EASY[randN];      break;
            case R.id.normal:   difficulty = NORMAL[randN];    break;
            case R.id.hard:     difficulty = HARD[randN];      break;
            default:
                finish();
                return super.onOptionsItemSelected(item);
        }
        newGame();
        return true;
    }
    //게임 시작 함수
    void gameStart() {
        while(true) {
            tempBoard = new int[9][9];  //임시로 숫자 넣을 보드
            finalBoard = new int[9][9]; //최종적으로 완성될 보드
            row = new boolean[9][10];   //9개 행, 1~9까지의 숫자
            col = new boolean[9][10];   //9개 열, 1~9까지의 숫자
            box = new boolean[9][10];   //9개 박스, 1~9까지의 숫자
            fillBoard(0,0,0,false);  //보드 스도쿠 규칙에 맞게 채우기
            makeBlanksInBoard(difficulty);     //보드에 난이도 만큼 빈칸 뚫기
            if(fillBoard(0,0,0,true)==1) break;
        }   //해가 1개가 나올때 까지 반복
        createSudokuButtons();      //스도쿠 생성
        createInputButtons();       //입력 버튼 생성
        boardStack = new BoardStack();

        baseTime = SystemClock.elapsedRealtime();
        handler.sendEmptyMessage(0);
        
        String s;
        if(VERYEASY[0] <= difficulty && difficulty <= VERYEASY[2])  s="매우 쉬움";
        else if(EASY[0] <= difficulty && difficulty <= EASY[2])      s="쉬움";
        else if(NORMAL[0] <= difficulty && difficulty <= NORMAL[2])    s="보통";
        else    s = "어려움";
        difficultyText.setText(s);
    }
    //스도쿠 규칙에 맞게 보드 채워주는 함수, 해의 개수 반환(최대 2개까지)
    int fillBoard(int rowNum, int colNum, int count, boolean ifCount) {
        if (colNum == 9) {
            rowNum += 1;
            colNum = 0;
            if (rowNum == 9)  return count+1;
        }
        if (tempBoard[rowNum][colNum] != 0) {  // 이미 채워진건 건너뛰기
            return fillBoard(rowNum, colNum+1, count,ifCount);
        }
        int boxNum = (rowNum/3)*3 + (colNum/3);
        int rand = (int)(Math.random()*9+1);
        for(int n=1; n<=9 && count<2 ; n++){  // count<2 :해가 2개 이상 되면 백트래킹 중단
            int num = (n+rand)%9+1; //숫자 골고루 들어가도록
            if(!row[rowNum][num] && !col[colNum][num] && !box[boxNum][num]){
                row[rowNum][num] = true; col[colNum][num] = true; box[boxNum][num] = true;
                tempBoard[rowNum][colNum] = num;
                count = fillBoard(rowNum,colNum+1,count,ifCount);
                if(ifCount==false && count == 1) return count;
                row[rowNum][num] = false; col[colNum][num] = false; box[boxNum][num] = false;
                tempBoard[rowNum][colNum] = 0;
            }
        }
        return count;
    }
    //랜덤으로 빈칸 뚫어주는 함수(난이도 만큼)
    void makeBlanksInBoard(int blanks) {
        for(int i=0; i<9; i++){
            for(int j=0; j<9; j++) {    //정답 확인을 위해서 답을 갖고있는 보드도 있어야함
                finalBoard[i][j] = tempBoard[i][j];
            }
        }
        int count=0;
        while(count<blanks){
            int rowNum = (int)(Math.random()*9);   int colNum = (int)(Math.random()*9);
            int boxNum = (rowNum/3)*3 + (colNum/3);
            if(tempBoard[rowNum][colNum]!=0){
                row[rowNum][tempBoard[rowNum][colNum]]=false;
                col[colNum][tempBoard[rowNum][colNum]]=false;
                box[boxNum][tempBoard[rowNum][colNum]]=false;
                tempBoard[rowNum][colNum]=0;
                count++;
            }
        }
    }
    //스도쿠 버튼 생성 함수
    void createSudokuButtons() {
        buttons = new SudokuButton[9][9];
        for(int i=0;i<9;i++){   //테이블에 9개 행 삽입
            TableRow tableRow = new TableRow(this);
            tableRow.setGravity(Gravity.CENTER);
            TableRow.LayoutParams layoutParams =
                    new TableRow.LayoutParams(
                            TableRow.LayoutParams.WRAP_CONTENT,
                            TableRow.LayoutParams.WRAP_CONTENT,
                            1.0f);
            table.addView(tableRow);
            for(int j=0;j<9;j++){   //각 테이블 열에 버튼 9개 삽입
                buttons[i][j] = new SudokuButton(this,i,j);
                tableRow.addView(buttons[i][j]);
                layoutParams.width  = 115;
                layoutParams.height = 115;
                buttons[i][j].setLayoutParams(layoutParams);
            }
        }
    }
    //지우기, 숫자, 힌트 입력 버튼 생성 함수
    void createInputButtons() {
        inputButtons = (RadioGroup) findViewById(R.id.inputBtns);
        for(int i=0 ; i<11 ; i++){
            RadioButton b = new RadioButton(this);
            b.setId(View.generateViewId());
            inputButtons.addView(b);
            b.setTextColor(Color.BLACK);
            b.setTextSize(Dimension.DP,defaultTextSize);
            if(i==0){
                b.setText("X");
                b.setChecked(true);
                b.setTextColor(Color.WHITE);
            }
            else if(i==10)  b.setText("H");
            else b.setText(Integer.toString(i));

            b.setButtonDrawable(null);
            b.setPadding(30,30,30,30);
            b.setBackgroundResource(R.drawable.selector_radio_button);
            b.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                    if(isChecked){
                        for(SudokuButton[] bRaw : buttons){
                            for(SudokuButton b : bRaw){
                                b.setOnClickListener(listener);
                            }
                        }
                        compoundButton.setTextColor(Color.WHITE);
                    }
                    else{
                        compoundButton.setTextColor(Color.BLACK);
                    }
                }
            });
        }
    }
    //새로운 스도쿠 만들어주는 함수
    void newGame(){
        hintCount = 0;
        table.removeAllViews();
        inputButtons.removeAllViews();
        gameStart();
    }
    //스도쿠가 맞게 완성됐는지 판단해서 참 거짓 반환하는 함수
    boolean isGameEnd(){
        boolean gameEnd = true;
        for(SudokuButton[] bRaw : buttons) {
            for (SudokuButton b : bRaw) {
                if ( !b.getText().equals(Integer.toString(b.value)) || b.isMemo)
                    gameEnd = false;
            }
        }
        return gameEnd;
    }
    //선택된 칸을 기준으로 고려해야 할 칸들 색깔 다르게 표시해주는 함수
    void check(SudokuButton me) {
        if(me == null){
            for(SudokuButton[] bRaw : buttons){
                for(SudokuButton sB : bRaw){
                    sB.setColor(DEFAULT);
                }
            }
            return;
        }
        String myText = (String)me.getText();
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                SudokuButton other = buttons[i][j];
                String othersText = (String)other.getText();

                if (other.x != me.x && other.y != me.y && other.box != me.box){
                    other.setColor(other.isLegal()?DEFAULT:WARN);
                    if(myText.equals(othersText) && !myText.equals("")
                            && !me.isMemo && !other.isMemo){
                        other.setColor(other.isLegal()?SAMENUM:WARN);
                    }
                    continue;
                }

                if(!me.isMemo && other.isMemo && othersText.contains(myText)){
                    other.setText(othersText.replace(myText,""));
                }

                other.setColor(other.isLegal()?CHECK:WARN);
                if(other.x==me.x && other.y==me.y)
                    other.setColor(other.isLegal()?SAMENUM:WARN);
            }
        }//스도쿠 완성했을 때
        if(isGameEnd()){
            handler.removeMessages(0);

            for(SudokuButton[] bRaw : buttons){
                for(SudokuButton b : bRaw){
                    b.setColor(CHECK);
                    b.setEnabled(false);
                }
            }

            String difficulty = (String)difficultyText.getText();
            String overTimeMsg = "";
            if(hour!=0)    overTimeMsg+=String.format("%d시간 ",hour);
            if(minute!=0)    overTimeMsg+=String.format("%d분 ",minute);
            if(second!=0)    overTimeMsg+=String.format("%d초",second);
            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setTitle("완성하셨군요! 축하드립니다!")
                    .setMessage(String.format("난이도: %s\n" +
                            "걸린 시간: %s\n" +
                            "사용힌트: %d개\n", difficulty, overTimeMsg, hintCount)
                            +"메뉴에서 새로 시작할 수 있습니다.")
                    .setPositiveButton("확인",null);
            AlertDialog dialog = builder.create();
            dialog.show();
        }
    }
    //버튼들 클릭 리스너 설정 함수
    void setButtonsClickListener() {
        //메모 스위치
        memoSwitch = (Switch)findViewById(R.id.memoBtn);
        memoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                for(SudokuButton[] bRaw : buttons){
                    for(SudokuButton sB : bRaw){
                        sB.setOnClickListener(listener);
                    }
                }
            }
        });
        //되돌리기 버튼
        undoBtn = (Button)findViewById(R.id.undoBtn);
        undoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isGameEnd()) return;
                if(boardStack.InputIndex ==1){
                    Toast.makeText(getApplicationContext(),
                            "더 이상 되돌릴 수 없습니다.",Toast.LENGTH_SHORT).show();
                }
                boardStack.pop();
            }
        });
        //모두 지우기 버튼
        eraseAllBtn = (Button)findViewById(R.id.eraseAllBtn);
        eraseAllBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isGameEnd()) return;
                for(SudokuButton[] bRaw : buttons){
                    for(SudokuButton sB : bRaw) {
                        if(!sB.isFinal)  sB.setText(null);
                    }
                }
                check(null);
                boardStack.push(buttons);
            }
        });
        //일시정지 버튼
        pauseBtn = (Button)findViewById(R.id.pauseBtn);
        pauseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGameEnd()) return;
                pauseTime = SystemClock.elapsedRealtime();
                handler.removeMessages(0);

                table.setVisibility(View.GONE);
                memoSwitch.setVisibility(View.GONE);
                inputButtons.setVisibility(View.GONE);
                undoBtn.setVisibility(View.GONE);
                pauseBtn.setVisibility(View.GONE);
                eraseAllBtn.setVisibility(View.GONE);

                resumeBtn.setVisibility(View.VISIBLE);
            }
        });
        //재개 버튼
        resumeBtn = (Button)findViewById(R.id.resumeBtn);
        resumeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                baseTime += SystemClock.elapsedRealtime() - pauseTime;
                handler.sendEmptyMessage(0);

                table.setVisibility(View.VISIBLE);
                memoSwitch.setVisibility(View.VISIBLE);
                inputButtons.setVisibility(View.VISIBLE);
                undoBtn.setVisibility(View.VISIBLE);
                pauseBtn.setVisibility(View.VISIBLE);
                eraseAllBtn.setVisibility(View.VISIBLE);

                resumeBtn.setVisibility(View.GONE);
            }
        });
    }
    //스도쿠 버튼들 클릭 리스너
    class SudokuButtonListener implements View.OnClickListener{
        @Override
        public void onClick(View view) {
            selected = (SudokuButton)view;
            int checkedInput = inputButtons.getCheckedRadioButtonId();
            String input = (String)((RadioButton)findViewById(checkedInput)).getText();
            //검정숫자 눌렀을때 혹은 지우기 버튼으로 빈칸 눌렀을 때
            if (selected.isFinal || input.equals("X") && selected.getText().equals("")) {
                check(selected);
                return;
            }
            //힌트 사용할 때
            if (input.equals("H")) {
                hintCount++;
                selected.setText(Integer.toString(selected.value));
                selected.setTypeface(Typeface.DEFAULT_BOLD);
                selected.setTextSize(Dimension.DP,defaultTextSize);
                selected.setTextColor(Color.BLACK);
                selected.isFinal = true;
                selected.isMemo = false;
                //힌트로 답이 나오면 되돌리기 해도 답이 지워지면 안되므로 보드 스택 전체 단계에 넣어줌
                for(int i = 0; i<boardStack.InputIndex; i++){
                    boardStack.boardsText[i][selected.x][selected.y] = Integer.toString(selected.value);
                    boardStack.boardsIsMemo[i][selected.x][selected.y] = selected.isMemo;
                    boardStack.boardsIsFinal[i][selected.x][selected.y] = true;
                }
                check(selected);
                return; //스택 낭비 막기 위해 바로 리턴
            }
            //지우기 버튼 사용했을 때
            if (input.equals("X")) selected.setText(null);
            //숫자 입력, 메모 스위치 켜져있을 때
            else if(memoSwitch.isChecked()){
                if(!selected.isMemo) selected.setText(null);    //메모가 아니면 지우고 시작
                String memo = ((String) selected.getText());
                if (memo.contains(input)) { //이미 입력한 숫자가 메모 돼있으면
                    memo = memo.replace(input, ""); //그 숫자만 지움
                } else {    //메모 안돼있으면
                    memo += input;  //메모에 입력한 숫자 추가
                }
                //메모 오름차순으로 정렬
                char[] temp = memo.toCharArray();
                Arrays.sort(temp);
                memo = new String(temp);
                //정렬된 메모로 크기와 색 달리해서 입력
                selected.setText(memo);
                selected.setTypeface(Typeface.DEFAULT_BOLD);
                selected.setTextSize(Dimension.DP,memoTextSize);
                selected.setTextColor(Color.rgb(21, 140, 57));//녹색
                selected.isMemo=true;
            }//숫자 입력, 메모 스위치 꺼져있을 때
            else {
                if(selected.isMemo) selected.setText(null);     //메모이면 지우고 시작
                if(selected.getText().equals(input)){
                    check(selected);
                    return;
                }
                else {
                    selected.setText(input);
                    selected.setTextColor(Color.BLUE);
                    selected.setTypeface(Typeface.DEFAULT_BOLD);
                    selected.setTextSize(Dimension.DP,defaultTextSize);
                    selected.isMemo=false;
                }
            }
            check(selected);
            boardStack.push(buttons);
        }
    }
    @SuppressLint("AppCompatCustomView")    //스도쿠 버튼
    class SudokuButton extends Button {
        int x,y; //좌표
        int box;
        int value;
        boolean isFinal;
        boolean isMemo;

        SudokuButton(Context context, int x, int y){
            super(context);
            this.x = x; this.y = y;
            value = finalBoard[x][y];
            box = (x/3)*3 + (y/3);

            int n = tempBoard[x][y];
            if(n==0)    setText(null);
            else{
                isFinal = true;
                setText(Integer.toString(n));
                setTextColor(Color.BLACK);
                setTypeface(Typeface.DEFAULT_BOLD);
                setTextSize(Dimension.DP,defaultTextSize);
            }
            setColor(DEFAULT);
            SudokuButton me = this;
            setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    check(me);
                }
            });
        }
        void setColor(int when){
            TypedArray array;
            if( (x==2 || x==5) && (y==2 || y==5) )
                array = getResources().obtainTypedArray(R.array.bottom_right);
            else if(x==2 || x==5)
                array = getResources().obtainTypedArray(R.array.bottom);
            else if(y==2 || y==5)
                array = getResources().obtainTypedArray(R.array.right);
            else
                array = getResources().obtainTypedArray(R.array.no);

            setBackground(array.getDrawable(when));
        }
        boolean isLegal(){
            String mine = (String)getText();
            for (int i = 0; i < 9; i++) {
                for (int j = 0; j < 9; j++) {
                    SudokuButton b = buttons[i][j];
                    String others = (String)b.getText();

                    if (i != x && j != y && b.box != box)   continue;

                    if(!others.equals("") && mine.contains(others)
                            && !(i==x&&j==y) && !b.isMemo){
                        return false;
                    }
                }
            }
            return true;
        }
    }
    //되돌리기할 때 필요한 보드상태 스택 형식으로 저장
    class BoardStack{
        String[][][]    boardsText;
        boolean[][][]     boardsIsMemo;
        boolean[][][]   boardsIsFinal;
        SudokuButton[]  selectedBtn;
        int InputIndex;

        BoardStack(){
            boardsText = new String[difficulty][9][9];
            boardsIsMemo = new boolean[difficulty][9][9];
            boardsIsFinal = new boolean[difficulty][9][9];
            selectedBtn = new SudokuButton[difficulty];
            push(buttons);
        }
        void push(SudokuButton[][] board){
            if(InputIndex == difficulty){
                for(int i=1;i<difficulty;i++){
                    for(int j=0;j<9;j++) {
                        for (int k = 0; k < 9; k++) {
                            boardsText[i-1][j][k] = boardsText[i][j][k];
                            boardsIsMemo[i-1][j][k] = boardsIsMemo[i][j][k];
                            boardsIsFinal[i-1][j][k] = boardsIsFinal[i][j][k];
                        }
                    }
                    selectedBtn[i-1]=selectedBtn[i];
                }
                InputIndex--;
            }
            for(int i=0;i<9;i++){
                for(int j=0;j<9;j++){
                    boardsText[InputIndex][i][j] = (String) board[i][j].getText();
                    boardsIsMemo[InputIndex][i][j] = board[i][j].isMemo;
                    boardsIsFinal[InputIndex][i][j] = board[i][j].isFinal;
                }
            }
            selectedBtn[InputIndex++] = selected;
        }
        void pop(){
            if(InputIndex ==1)   return;
            InputIndex--;
            for(int i=0;i<9;i++){
                for(int j=0;j<9;j++){
                    SudokuButton sB = buttons[i][j];
                    boardsText[InputIndex][i][j] = null;
                    boardsIsMemo[InputIndex][i][j] = false;
                    boardsIsFinal[InputIndex][i][j] = false;
                    selectedBtn[InputIndex] = null;

                    sB.setText(boardsText[InputIndex -1][i][j]);
                    sB.isFinal = boardsIsFinal[InputIndex -1][i][j];
                    sB.isMemo = boardsIsMemo[InputIndex -1][i][j];
                    sB.setTextSize(Dimension.DP,sB.isMemo?memoTextSize:defaultTextSize);
                    if(sB.isMemo)   sB.setTextColor(Color.rgb(21, 140, 57));
                    else            sB.setTextColor(sB.isFinal?Color.BLACK:Color.BLUE);
                }
            }
            check(selectedBtn[InputIndex -1]);
        }
    }
}