// SPDX-License-Identifier: Apache-2.0

package circt.stage.phases

import circt.stage.{
  CIRCTHandover,
  CIRCTTargetAnnotation
}

import firrtl.{
  AnnotationSeq,
  Emitter,
  SystemVerilogEmitter
}
import firrtl.annotations.Annotation
import firrtl.options.{
  Dependency,
  OptionsException,
  Phase
}
import firrtl.stage.OutputFileAnnotation

/** Check properties of an [[AnnotationSeq]] to look for errors before running CIRCT. */
class Checks extends Phase {

  override def prerequisites = Seq.empty
  override def optionalPrerequisites = Seq(Dependency[circt.stage.phases.AddDefaults])
  override def optionalPrerequisiteOf = Seq(Dependency[circt.stage.phases.CIRCT])
  override def invalidates(a: Phase) = false

  override def transform(annotations: AnnotationSeq): AnnotationSeq = {
    val target, handover = collection.mutable.ArrayBuffer[Annotation]()

    annotations.foreach {
      case a: CIRCTTargetAnnotation => target += a
      case a: CIRCTHandover => handover += a
      case _ =>
    }

    if (target.size != 1) {
      throw new OptionsException("Exactly one CIRCT target must be specified")
    }

    if (handover.size != 1) {
      throw new OptionsException("Exactly one handover must be specified")
    }

    annotations
  }

}
