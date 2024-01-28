// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/04/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
//
// This program only needs to handle arguments that satisfy
// R0 >= 0, R1 >= 0, and R0*R1 < 32768.

@R1 
D = M
@n
M = D //R0
@i //iterations
M = 1
@sum
M = 0
(LOOP)
@i
D = M
@n
D = D - M
@STOP
D;JGT //if i>n than goto STOP

@sum 
D = M
@R0
D = D + M 
@sum 
M = D //sum = sum + R0
@i 
M = M + 1
@LOOP
0;JMP

(STOP)
@sum 
D = M
@R2
M = D //R2 = R0*R1

(END)
@END
0;JMP

