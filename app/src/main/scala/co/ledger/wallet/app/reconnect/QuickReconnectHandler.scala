/**
  *
  * QuickReconnectHandler
  * Ledger wallet
  *
  * Created by Pierre Pollastri on 18/04/16.
  *
  * The MIT License (MIT)
  *
  * Copyright (c) 2015 Ledger
  *
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  *
  * The above copyright notice and this permission notice shall be included in all
  * copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  *
  */
package co.ledger.wallet.app.reconnect

import android.app.Activity
import co.ledger.wallet.core.base.DeviceActivity
import co.ledger.wallet.core.device.Device

import scala.concurrent.Future
import co.ledger.wallet.core.concurrent.ExecutionContext.Implicits.ui

trait QuickReconnectHandler {
  def show(): QuickReconnectHandler
  def cancel(): QuickReconnectHandler
  def device: Future[Device]
}

object QuickReconnectHandler {
  import co.ledger.wallet.core.device.DeviceManager.ConnectivityTypes._

  def apply(activity: DeviceActivity): Future[QuickReconnectHandler] = {
    activity.deviceManagerService flatMap {(manager) =>
      manager.lastConnectedDevice() map {(device) =>
        new AlreadyConnectedDeviceQuickReconnectHandler(device)
      } recoverWith {
        case all =>
          manager.lastConnectedDeviceInfo() map {
            case (factory, info) =>
              factory.connectivityType match {
                case Ble => null
                case Usb => new UsbQuickReconnectHandler(activity, factory, info)
                case Tee => null
                case Nfc => null
              }
          }
          }
      }

  }

  private class AlreadyConnectedDeviceQuickReconnectHandler(d: Device) extends QuickReconnectHandler{
    override def show(): QuickReconnectHandler = this

    override def cancel(): QuickReconnectHandler = this

    override def device: Future[Device] = Future.successful(d)
  }

}