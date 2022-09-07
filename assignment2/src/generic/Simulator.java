package generic;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import generic.Instruction.OperationType;
import generic.Operand.OperandType;


public class Simulator {
		
	static FileInputStream inputcodeStream = null;
	
	public static void setupSimulation(String assemblyProgramFile)
	{	
		int firstCodeAddress = ParsedProgram.parseDataSection(assemblyProgramFile);
		ParsedProgram.parseCodeSection(assemblyProgramFile, firstCodeAddress);
		ParsedProgram.printState();
	}

	public static void assemble(String objectProgramFile)
	{
		//TODO your assembler code

        try {
            //1. open the objectProgramFile in binary mode
            FileOutputStream outputStream = new FileOutputStream(objectProgramFile);

            //2. write the firstCodeAddress to the file
            outputStream.write(ParsedProgram.firstCodeAddress);

            //3. write the data to the file
            for (int i = 0; i < ParsedProgram.data.size(); i++) {
                outputStream.write(ParsedProgram.data.get(i));
            }

            //4. assemble one instruction at a time, and write to the file
            for (int i = 0; i < ParsedProgram.code.size(); i++) {
                Instruction instruction = ParsedProgram.code.get(i);

                String instructionString;
                Integer instructionInt = 0;

                // Find opcode string
                String opcode = Integer.toBinaryString(instruction.getOperationType().ordinal());
                while (opcode.length() < 5) {
                    opcode = "0" + opcode;
                }

                switch (getInstructionFormat(instruction)) {
                    // R3 Format
                    case 3: {
                        // Find rs1 string
                        Operand operand = instruction.getSourceOperand1();
                        if (operand.getOperandType() != OperandType.Register) {
                            Misc.printErrorAndExit(String.format("[Assembly Error]: Line %d - Source operand 1 is not a register in R3 format instruction", i));
                        }
                        String rs1 = Integer.toBinaryString(instruction.getSourceOperand1().getValue());
                        while (rs1.length() < 5) {
                            rs1 = "0" + rs1;
                        }

                        // Find rs2 string
                        operand = instruction.getSourceOperand2();
                        if (operand.getOperandType() != OperandType.Register) {
                            Misc.printErrorAndExit(String.format("[Assembly Error]: Line %d - Source operand 2 is not a register in R3 format instruction", i));
                        }
                        String rs2 = Integer.toBinaryString(instruction.getSourceOperand2().getValue());
                        while (rs2.length() < 5) {
                            rs2 = "0" + rs2;
                        }

                        // Find rd string
                        operand = instruction.getDestinationOperand();
                        if (operand.getOperandType() != OperandType.Register) {
                            Misc.printErrorAndExit(String.format("[Assembly Error]: Line %d - Destination operand is not a register in R3 format instruction", i));
                        }
                        String rd = Integer.toBinaryString(instruction.getDestinationOperand().getValue());
                        while (rd.length() < 5) {
                            rd = "0" + rd;
                        }

                        instructionString = opcode + rs1 + rs2 + rd;
                        while(instructionString.length() < 32) {
                            instructionString = "0" + instructionString;
                        }
                        instructionInt = Integer.valueOf(instructionString, 2);
                        
                        break;
                    }

                    // R2I Format
                    case 2:

                        // Find rs1 string 
                        Operand operand = instruction.getSourceOperand1();
                        if (operand.getOperandType() != OperandType.Register) {
                            Misc.printErrorAndExit(String.format("[Assembly Error]: Line %d - Source operand 1 is not a register in R2I format instruction", i));
                        }

                        String rs1 = Integer.toBinaryString(instruction.getSourceOperand1().getValue());
                        while (rs1.length() < 5) {
                            rs1 = "0" + rs1;
                        }

                        // Find rd string
                        operand = instruction.getDestinationOperand(); 
                        if (operand.getOperandType() != OperandType.Register) {
                            Misc.printErrorAndExit(String.format("[Assembly Error]: Line %d - Destination operand is not a register in R2I format instruction", i));
                        }
                        String rd = Integer.toBinaryString(instruction.getDestinationOperand().getValue());
                        while (rd.length() < 5) {
                            rd = "0" + rd;
                        }

                        // Find imm string
                        Operand immediate = instruction.getSourceOperand2();
                        if(immediate.getOperandType() != OperandType.Immediate) {
                            Misc.printErrorAndExit(String.format("[Assembly Error]: Line %d - Source operand 2 is not an immediate in R2I format instruction", i));
                        }
                        String imm = Integer.toBinaryString(instruction.getSourceOperand2().getValue());
                        while (imm.length() < 17) {
                            imm = "0" + imm;
                        }

                        instructionString = opcode + rs1 + rd + imm;
                        instructionInt = Integer.valueOf(instructionString, 2);

                    // RI Format
                    case 1:

                    if(instruction.getOperationType() == OperationType.jmp){

                        String rd1 = "";
                        String imm1 = "";
                        operand = instruction.getDestinationOperand();
                        if (operand.getOperandType() == OperandType.Register) {
                        rd1 = Integer.toBinaryString(instruction.getDestinationOperand().getValue());
                            while (rd1.length() < 5) {
                                rd1 = "0" + rd1;
                            }
                        
                        imm1 = "";
                            for (int k=0; k<22; k++){
                                imm1 = imm1 + "0";
                            }
                        }

                        else if(operand.getOperandType() == OperandType.Immediate)
                        {
                            imm1 = Integer.toBinaryString(instruction.getDestinationOperand().getValue());
                            while (imm1.length() < 22) {
                                imm1 = "0" + imm1;
                            }
                            rd1 = "";
                            for (int k=0; k<5; k++){
                                rd1 = rd1 + "0";
                            }
                        }
                        


                        instructionString = opcode + rd1 + imm1;
                        instructionInt = Integer.valueOf(instructionString, 2);

                    } else {
                        String rest = "";
                        for(int j=0; j<27; j++) {
                            rest = rest + '0';
                        }
                        instructionString = opcode + rest;
                        instructionInt = Integer.valueOf(instructionString, 2);
                    }
                        


                    
                    default: {
                        Misc.printErrorAndExit("[Assembly Error]: Invalid instruction type");
                    }
                }

                outputStream.write(instructionInt);
            }

            //5. close the file
            outputStream.close();
        }
        catch (Exception e) {
            Misc.printErrorAndExit("[Assembly Error]: " + e.getMessage());
        }
	}
	
    private static int getInstructionFormat(Instruction instruction) {
        switch(instruction.getOperationType()) {
            //R3 type
            case add : 
            case sub : 
            case mul : 
            case div : 
            case and : 
            case or : 
            case xor : 
            case slt : 
            case sll : 
            case srl : 
            case sra : return 3;

            //R2I type
            case addi :
            case subi :
            case muli :
            case divi : 
            case andi : 
            case ori : 
            case xori : 
            case slti : 
            case slli : 
            case srli : 
            case srai :
            case load :
            case store :
            
            case beq : 
            case bne : 
            case blt : 
            case bgt : return 2;

            //RI type :
            case jmp :
            
            case end : return 1;

            default : return 0;
        }
    }
	
}