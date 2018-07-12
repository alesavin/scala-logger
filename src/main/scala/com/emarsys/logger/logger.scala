package com.emarsys

import cats.data.ReaderT
import cats.{Applicative, MonadError}
import com.emarsys.logger.internal.{LoggingContextMagnet, ToMapRec}
import shapeless.{HList, LabelledGeneric, Lazy}

package object logger {

  type Logged[F[_], A] = ReaderT[F, LoggingContext, A]

  object syntax {
    import cats.syntax.applicativeError._
    import cats.syntax.apply._
    import cats.syntax.flatMap._

    def log[F[_]](implicit logging: Logging[F]): Logging[F] = logging

    implicit class LogOps[F[_]: Logging: MonadError[?[_], Throwable], A](fa: F[A]) {
      def logFailure(implicit ctx: LoggingContext): F[A] = fa onError {
        case e: Throwable =>
          log.error(e)
      }

      def logFailure(msg: => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa onError {
        case e: Throwable =>
          log.error(e, msg)
      }

      def logFailure(createMsg: Throwable => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa onError {
        case e: Throwable =>
          log.error(e, createMsg(e))
      }

      def logSuccess(msg: => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa <* log.info(msg)

      def logSuccess(createMsg: A => String)(implicit wlc: LoggingContextMagnet[F]): F[A] = fa flatTap { value =>
        log.info(createMsg(value))
      }
    }

    implicit class LogConverter[F[_], A](fa: F[A]) {
      def toLogged: Logged[F, A] = withLoggingContext(_ => fa)
    }

    def withLoggingContext[F[_], A](block: LoggingContext => F[A]): Logged[F, A] = ReaderT(block)

    def getLoggingContext[F[_]: Applicative]: Logged[F, LoggingContext] = ReaderT.ask[F, LoggingContext]

    def withExtendedLoggingContextRT[F[_], A](params: (String, Any)*)(block: LoggingContext => F[A]): Logged[F, A] =
      ReaderT.local[F, A, LoggingContext](_.addParameters(params: _*))(ReaderT(block))

    def withExtendedLoggingContext[F[_]: Context, A](params: (String, Any)*)(fa: => F[A]): F[A] = {
      Context[F].local(_.addParameters(params: _*))(fa)
    }

    implicit class AnyToLoggingContextOps[A](a: A) {
      def toCtx[L <: HList](transactionId: String)(implicit gen: LabelledGeneric.Aux[A, L],
                                                   toMap: Lazy[ToMapRec[L]]): LoggingContext = {
        LoggingContext.fromData(a, transactionId)
      }

      def toCtx[L <: HList](transactionIdExtractor: A => String)(implicit gen: LabelledGeneric.Aux[A, L],
                                                                 toMap: Lazy[ToMapRec[L]]): LoggingContext = {
        LoggingContext.fromData(a, transactionIdExtractor(a))
      }
    }
  }
}
