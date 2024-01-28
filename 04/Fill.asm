// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Fill.asm

// Runs an infinite loop that listens to the keyboard input.
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel;
// the screen should remain fully black as long as the key is pressed. 
// When no key is pressed, the program clears the screen, i.e. writes
// "white" in every pixel;
// the screen should remain fully clear as long as no key is pressed.
(INIT) // set curReg to the first row register and lastR to the last row 
@KBD 
D = A - 1
@lastR
M = D // lastR == KBD - 1
@SCREEN
D = A
@curReg
M = D //curReg = screen

(COND) //checking whether keyboard is pressed or not
@KBD
D = M
@WHITE 
D;JEQ //WHITE
@BLACK
0;JMP //BLACK

(WHITE) //make the screen go fully white
@curReg
D = M // color
A = D 
M = 0
@curReg
D = M
D = D + 1
M = D //curReg++
@curReg // did you finished?
D = M
@lastR
D = D - M
@INIT
D;JGT //if curReg > lastR than goto END = screen is WHITE
@COND
0;JMP //goto COND

(BLACK) //make the screen go fully black
@curReg
D = M // color
A = D 
M = -1
@curReg
D = M
D = D + 1
M = D //curReg++
@curReg // did you finished?
D = M
@lastR
D = D - M
@INIT
D;JGT //if curReg > lastR than goto END = screen is BLACK
@COND
0;JMP //goto COND
