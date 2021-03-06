package io.appium.android.bootstrap.handler;

import io.appium.android.bootstrap.AndroidCommand;
import io.appium.android.bootstrap.AndroidCommandResult;
import io.appium.android.bootstrap.CommandHandler;
import io.appium.android.bootstrap.Logger;
import io.appium.android.bootstrap.OrientationEnum;

import java.util.Hashtable;

import org.json.JSONException;

import android.os.RemoteException;

import com.android.uiautomator.core.UiDevice;

/**
 * This handler is used to get or set the orientation of the device.
 * 
 */
public class Orientation extends CommandHandler {

  /*
   * @param command The {@link AndroidCommand} used for this handler.
   * 
   * @return {@link AndroidCommandResult}
   * 
   * @throws JSONException
   * 
   * @see io.appium.android.bootstrap.CommandHandler#execute(io.appium.android.
   * bootstrap.AndroidCommand)
   */
  @Override
  public AndroidCommandResult execute(final AndroidCommand command)
      throws JSONException {

    final Hashtable<String, Object> params = command.params();
    if (params.containsKey("orientation")) {
      // Set the rotation

      final String orientation = (String) params.get("orientation");
      try {
        return handleRotation(orientation);
      } catch (final RemoteException e) {
        return getErrorResult("Unable to rotate screen: " + e.getMessage());
      }
    } else {
      // Get the rotation
      return getRotation();
    }

  }

  /**
   * Returns the current rotation
   * 
   * @return {@link AndroidCommandResult}
   */
  private AndroidCommandResult getRotation() {
    String res = null;
    final UiDevice d = UiDevice.getInstance();
    final OrientationEnum currentRotation = OrientationEnum.fromInteger(d
        .getDisplayRotation());
    Logger.debug("Current rotation: " + currentRotation);
    switch (currentRotation) {
      case ROTATION_0:
      case ROTATION_180:
        res = "PORTRAIT";
        break;
      case ROTATION_90:
      case ROTATION_270:
        res = "LANDSCAPE";
        break;
    }

    if (res != null) {
      return getSuccessResult(res);
    } else {
      return getErrorResult("Get orientation did not complete successfully");
    }
  }

  /**
   * Set the desired rotation
   * 
   * @param orientation
   *          The rotation desired (LANDSCAPE or PORTRAIT)
   * @return {@link AndroidCommandResult}
   * @throws RemoteException
   */
  private AndroidCommandResult handleRotation(final String orientation)
      throws RemoteException {
    final UiDevice d = UiDevice.getInstance();
    OrientationEnum desired;
    OrientationEnum current = OrientationEnum.fromInteger(d
        .getDisplayRotation());

    Logger.debug("Desired orientation: " + orientation);
    Logger.info("Current rotation: " + current);

    if (orientation.equalsIgnoreCase("LANDSCAPE")) {
      switch (current) {
        case ROTATION_0:
          d.setOrientationRight();
          desired = OrientationEnum.ROTATION_90;
          break;
        case ROTATION_180:
          d.setOrientationRight();
          desired = OrientationEnum.ROTATION_270;
          break;
        default:
          return getSuccessResult("Already in landscape mode.");
      }
    } else {
      switch (current) {
        case ROTATION_90:
          d.setOrientationRight();
          desired = OrientationEnum.ROTATION_180;
          break;
        case ROTATION_270:
          d.setOrientationNatural();
          desired = OrientationEnum.ROTATION_0;
          break;
        default:
          return getSuccessResult("Already in portrait mode.");
      }
    }
    current = OrientationEnum.fromInteger(d.getDisplayRotation());
    if (current != desired) {
      return getErrorResult("Set the orientation, but app refused to rotate.");
    }
    return getSuccessResult("Rotation (" + orientation + ") successful.");
  }
}
