//简单有限状态机
import chisel3._
import chisel3.util._

class SimpleFsm extends Moudle{
    val io = IO(new Bundle{
        val badEvent = Input(Bool())
        val clear = Input(Bool())
        val ringBell = Output(Bool())
    })
    //三种状态
    val green :: orange :: red :: Nil = Enum(3) //Nil表示结束符
    //状态寄存器
    val stateReg = RegInit(green)
    //接下来的逻辑状态
    switch(stateReg){
        is(green){
            when(io.badEvent){
                stateReg := orange
            }
        }
        is(orange){
            when(io.badEvent){
                stateReg := red
            }.elsewhen(io.clear){
                stateReg := green
            }
        }
        is(red){
            when(io.clear){
                stateReg := green
            }
        }
    }
    io.ringBell := stateReg === red
}

val risingEdge = din & !RegNext(din)//普通的上升沿检测
//Mealy型（输出值取决于当前状态与输入值）上升沿检测状态机 chisel-book Figure 10.5 
//10.5图中其中 in/out 表示输入in输出out，圆圈内为当前状态
import chisel3._
import chisel3.util._

class RisingEdge extends Moudle{
    val io=IO(new Bundle{
        val din=Input(Bool())
        val risingEdge=Output(Bool())
    })
    //两种状态
    val zero :: one :: Nil = Enum(2)
    //寄存器状态
    val stateReg = RegInit(zero)
    //默认输出
    io.risingEdge := false.B
    //下个输出状态
    switch(stateReg){
        is(zero){
            when(io.din){
                stateReg := one
                io.risingEdge := true.B
            }
        }
        is(one){
            when(!io.din){
                stateReg := zero
            }
        }
    }
}

//Moore型（输出值只取决于当前状态）上升沿检测状态机 chisel-book Figure 10.6
 import chisel3._
import chisel3.util._

class RisingMooreEdge extends Moudle{
    val io=IO(new Bundle{
        val din=Input(Bool())
        val risingEdge=Output(Bool())
    })
    //三种状态
    val zero :: puls :: one :: Nil = Enum(3)
    //寄存器状态
    val stateReg = RegInit(zero)
    //下个输出状态
    switch(stateReg){
        is(zero){
            when(io.din){
                stateReg := puls
            }
        }
        is(puls){
            when(io.din){
                stateReg := one
            }.otherwise{
                stateReg := zero
            }
        }
        is(one){
            when(!io.din){
                stateReg := zero
            }
        }
    }
    //输出逻辑
    io.risingEdge := stateReg === puls
}