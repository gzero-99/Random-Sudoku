# Random-Sudoku
Random Sudoku game android application 

# App Icon
<img width="323" alt="스크린샷 2021-12-19 오전 11 48 38" src="https://user-images.githubusercontent.com/70897603/146661888-4050733d-d5d8-4a77-9725-333bc9de1ff7.png">

# Start Screen
<img width="246" alt="스크린샷 2021-12-22 오전 12 01 17" src="https://user-images.githubusercontent.com/70897603/146951351-2dcd8a15-e35a-4a3a-997e-a8c525ab7a83.png">

# Choose game difficulty
![image](https://user-images.githubusercontent.com/70897603/147357909-eccc402b-9247-46d9-a442-741611f9e6d3.png)

# Choose game difficulty in game and Restart a new game
![image](https://user-images.githubusercontent.com/70897603/147384823-132a4983-5498-4851-92a9-7243c25abae3.png)

# Game Screen
<img width="300" alt="스크린샷 2021-12-23 오전 9 45 46" src="https://user-images.githubusercontent.com/70897603/147170930-f4f34a31-efb5-4c6c-8d51-b9ae4a255f9b.png">

# Timer
![image](https://user-images.githubusercontent.com/70897603/147402899-5ab9af56-86cc-4eaa-be64-7f28cf98019d.png)

# Memo
<img width="253" alt="스크린샷 2021-12-20 오전 11 35 42" src="https://user-images.githubusercontent.com/70897603/146703802-1bf870f7-7a6b-40af-bd49-42cf1ffdbb41.png">
toogle button을 이용하여 메모기능을 활성화/비활성화 가능

# Stop for a while
<img width="273" alt="스크린샷 2021-12-21 오전 10 42 55" src="https://user-images.githubusercontent.com/70897603/146856528-3ad8c8ae-8afb-4cd3-9b41-6b861635a84b.png">
게임 도중 일시 정지 시, 화면을 감추고 타이머를 정지

# Victory
![ezgif com-gif-maker](https://user-images.githubusercontent.com/70897603/146103530-b32806c3-59c9-4226-937a-5941133b526c.gif)


![image](https://user-images.githubusercontent.com/70897603/147412744-3b89a457-4239-48d4-82c2-f68caf12883a.png)

game finished dialog

# Backtracking code to generate puzzle

게임 시작 화면으로 넘어오면 9x9 보드 칸에 임의로 숫자 값들을 모두 넣어주는데 스도쿠 규칙에 따라 같은 행, 같은 열, 3*3 박스 내에 같은 숫자가 존재하지 않도록 한다.
이 조건을 만족하는 스도쿠 퍼즐보드를 생성하기 위해서 백트래킹 알고리즘을 사용하였다.

첫번째 행부터 채우며 다음 행으로 넘어가는데, 각 줄의 행과 열, 3*3 박스에 중복된 숫자가 들어 가 있지 않으면 그 값으로 채운다. 그리고 ifCount를 false로 설정하여 해가 1개만 존재하여도 해 당 함수를 끝내도록 하였다. 그 후 난이도에 맞춰 랜덤으로 빈칸을 만들어준다.
수월한 정답 확인을 위하여 랜덤으로 빈칸을 만들고 난 후의 보드에 들어갈 수 있는 해가 1개만 나오도록 설정했다. 이를 위해 ifCount를 true로 설정하여 fillBoard부분에서 해의 개수가 1개 이상
이어도 다음 해를 탐색하도록 하여 나올 수 있는 총 해의 개수를 구한다. 그리고 이 값이 1일 경 우에만 무한루프를 끝내도록 하였고, 그렇지 않은 경우 다시 실행하도록 설정했다.

# Thanks for
![image](https://user-images.githubusercontent.com/70897603/147526021-068bf2f7-79aa-4465-beb8-100a54afb8a7.png)
