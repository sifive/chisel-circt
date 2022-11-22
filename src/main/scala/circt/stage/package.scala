// SPDX-License-Identifier: Apache-2.0

package circt

import circt.stage.{CIRCTOption, CIRCTTargetAnnotation, ChiselStage, PreserveAggregate}

import chisel3.RawModule
import firrtl.AnnotationSeq
import firrtl.options.OptionsView
import circt.stage.FirtoolOption
import firrtl.stage.{FirrtlFileAnnotation, FirrtlOption, OutputFileAnnotation}

import java.io.File

package object stage {

  implicit object CIRCTOptionsView extends OptionsView[CIRCTOptions] {

    def view(annotations: AnnotationSeq): CIRCTOptions =
      annotations.collect {
        case a: CIRCTOption          => a
        case a: FirrtlOption         => a
        case a: FirrtlFileAnnotation => a
      }
        .foldLeft(new CIRCTOptions()) { (acc, c) =>
          c match {
            case FirrtlFileAnnotation(a)  => acc.copy(inputFile = Some(new File(a)))
            case OutputFileAnnotation(a)  => acc.copy(outputFile = Some(new File(a)))
            case CIRCTTargetAnnotation(a) => acc.copy(target = Some(a))
            case PreserveAggregate(a)     => acc.copy(preserveAggregate = Some(a))
            case FirtoolOption(a)         => acc.copy(firtoolOptions = acc.firtoolOptions :+ a)
            case _                        => acc
          }
        }

  }

}

object getSystemVerilogString {

  /** Returns a string containing the Verilog for the module specified by
    * the target.
    * @param gen a call-by-name Chisel module
    * @param args additional command line arguments to pass to Chisel
    * @param firtoolOpts additional [[circt.stage.FirtoolOption]] to pass to firtool
    * @return a string containing the SystemVerilog output
    */
  def apply(gen: => RawModule, args: Array[String] = Array.empty, firtoolOpts: Array[String] = Array.empty): String =
    ChiselStage.emitSystemVerilog(
      gen,
      (new circt.stage.ChiselStage).shell.parse(args) ++ firtoolOpts.map(FirtoolOption(_))
    )
}

object emitSystemVerilog {

  /** Compile a Chisel circuit to SystemVerilog with file output
    * @param gen a call-by-name Chisel module
    * @param args additional command line arguments to pass to Chisel
    * @param firtoolOpts additional command line options to pass to firtool
    */
  def apply(gen: => RawModule, args: Array[String] = Array.empty, firtoolOpts: Array[String] = Array.empty) = {
    ChiselStage.emitSystemVerilogFile(gen, args, AnnotationSeq(firtoolOpts.map(FirtoolOption(_))))
  }
}

object emitSystemVerilogSplit {

  /** Compile a Chisel circuit to SystemVerilog with one file output for each module
    * @param gen a call-by-name Chisel module
    * @param args additional command line arguments to pass to Chisel
    * @param firtoolOpts additional command line options to pass to firtool
    */
  def apply(gen: => RawModule, args: Array[String] = Array.empty, firtoolOpts: Array[String] = Array.empty) = {
    ChiselStage.emitSystemVerilogFile(
      gen,
      args,
      AnnotationSeq(
        firtoolOpts.map(FirtoolOption(_)) ++ Seq(firrtl.EmitAllModulesAnnotation(classOf[firrtl.SystemVerilogEmitter]))
      )
    )
  }
}
