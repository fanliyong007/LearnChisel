//外界异步信号同步输入 Figure 9.1
val btnSync = RegNext(RegNext(btn))

//防抖动
val FAC = 100000000/1000
val btnDebReg = Reg(Bool())
val cntReg := cntReg + 1.U
when(tick){
    cntReg := 0.U
    btnDebReg := btnSync
}

//输入信号滤波
val shiftReg = RegInit(0.U(3.W))
when(tick){
    shiftReg := Cat(shiftReg(1,0),btnDebReg)
}
val btnClean = (shiftReg(2)&shiftReg(1))|(shiftReg(2)&shiftReg(0))|(shiftReg(1)&shiftReg(0))
val risingEdge = btnClean & !RegNext(btnClean)
val reg = RegInit(0.U(8.W))
when(risingEdge){
    reg := reg + 1.U
}

//使用函数加速设计
def sync(v: Bool) = RegNext(RegNext(v))
def rising(v: Bool)= v & !RegNext(v)
def tickGen(fac: Int)={
    val reg=RegInit(0.U(log2Up(fac).W))
    val tick= reg === (fac-1).U
    reg := Mux(tick,0.U,reg+1.U)
    tick
}
def filter(v:Bool,t:Bool)={
    val reg=RegInit(0.U(3.W))
    when(t){
        reg := Cat(reg(1,0),v)
    }
    (reg(2)&reg(1))|(reg(2)&reg(0))|(reg(1)&reg(0))
}
val btnSync=sync(btn)
val tick=tickGen(fac)
val btnDeb=Reg(Bool())
when(tick){
    btnDeb := btnSync
}
val btnClean = filter(btnDeb,tick)  
val risingEdge = rising(btnClean)
val reg = RegInit(0.U(8.W))
when(risingEdge){
    reg := reg + 1.U
}