package com.emarsys.logger

import cats.Monad

import scala.annotation.implicitNotFound

@implicitNotFound(
  msg =
    "Unable to produce a LoggingContext. You might pass \nan (implicit ctx: LoggingContext) to your method \nor use the Context typeclass if ${F} is a monad.")
trait LoggingContextMagnet[F[_]] {
  def apply[Result](f: LoggingContext => F[Unit]): F[Unit]
}

object LoggingContextMagnet {

  implicit def fromImplicitLoggingContext[F[_]](implicit context: LoggingContext): LoggingContextMagnet[F] =
    new LoggingContextMagnet[F] {
      override def apply[Result](f: LoggingContext => F[Unit]): F[Unit] = f(context)
    }

  implicit def fromLoggingContext[F[_]](context: LoggingContext): LoggingContextMagnet[F] =
    new LoggingContextMagnet[F] {
      override def apply[Result](f: LoggingContext => F[Unit]): F[Unit] = f(context)
    }

  implicit def fromImplicitContextAndMonadTypeclass[F[_]](implicit C: Context[F],
                                                          M: Monad[F]): LoggingContextMagnet[F] =
    new LoggingContextMagnet[F] {
      override def apply[Result](f: LoggingContext => F[Unit]): F[Unit] = M.flatMap(C.ask)(f)
    }

}