package de.hsa.games.fatsquirrel.botimpl;

import de.hsa.games.fatsquirrel.botapi.ControllerContext;
import de.hsa.games.fatsquirrel.botapi.OutOfViewException;
import de.hsa.games.fatsquirrel.core.EntityType;
import de.hsa.games.fatsquirrel.core.XY;
import de.hsa.games.fatsquirrel.util.XYSupport;

import java.util.logging.Logger;

public class BotHelper {

    private static final Logger logger = Logger.getLogger("BotHelper");

    public static XY moveToNearestGoodEntity(ControllerContext context) {
        XY moveDirection = XY.ZERO_ZERO;
        XY nearestMasterBot = nearestEntity(context, EntityType.MASTER_SQUIRREL_BOT);
        XY nearestMaster = nearestEntity(context, EntityType.HAND_OPERATED_MASTER_SQUIRREL);
        XY nearestMini = (nearestEntity(context, EntityType.MINI_SQUIRREL));
        XY nearestMiniBot = (nearestEntity(context, EntityType.MINI_SQUIRREL_BOT));
        XY nearestBP = nearestEntity(context, EntityType.BAD_PLANT);
        XY nearestBB = nearestEntity(context, EntityType.BAD_BEAST);
        XY nearestGB = nearestEntity(context, EntityType.GOOD_BEAST);
        XY nearestGP = nearestEntity(context, EntityType.GOOD_PLANT);
        XY nearestWW = nearestEntity(context, EntityType.WALL);
        XY nearestPositive;
        if (nearestGB.distanceFrom(context.locate()) < nearestGP.distanceFrom(context.locate()))
            nearestPositive = nearestGB;
        else nearestPositive = nearestGP;

        if (context.locate().distanceFrom(nearestBB) < 1) {
            logger.finer("flee from badBeast");
            if ((context.locate().distanceFrom(nearestPositive) > context.locate().distanceFrom(nearestBB))) {
                moveDirection = XYSupport.decreaseDistance(nearestBB, context.locate());
            }
        } else if ((context.locate().distanceFrom(nearestPositive)) < 16) {
            logger.finer("shrink the distance between Squirrel and positiveEntities");
            moveDirection = XYSupport.decreaseDistance(context.locate(), nearestPositive);
        } else if ((context.locate().distanceFrom(nearestBP)) < 1) {
            moveDirection = XYSupport.decreaseDistance(nearestBP, context.locate());
        } else if ((context.locate().distanceFrom(nearestWW)) < 1) {
            moveDirection = XYSupport.decreaseDistance(nearestWW, context.locate());
        } else if ((context.locate().distanceFrom(nearestMiniBot)) < 16) {
            moveDirection = XYSupport.decreaseDistance(context.locate(), nearestMiniBot);
        } else if ((context.locate().distanceFrom(nearestMini)) < 16) {
            moveDirection = XYSupport.decreaseDistance(context.locate(), nearestMini);
        } else if ((context.locate().distanceFrom(nearestMasterBot)) < 2) {
            moveDirection = XYSupport.decreaseDistance(nearestMasterBot, context.locate());
        } else if ((context.locate().distanceFrom(nearestMaster)) < 2) {
            moveDirection = XYSupport.decreaseDistance(nearestMaster, context.locate());
        } else {
            if ((context.locate().distanceFrom(new XY((context.getViewUpperRight().getX() / 2), (context.getViewLowerLeft().getY() / 2)))) <
                    (context.locate().distanceFrom(new XY((context.getViewLowerLeft().getX() / 2), (context.getViewUpperRight().getY() / 2))))) {
                moveDirection = XYSupport.decreaseDistance(context.locate(), new XY(context.getViewLowerLeft().getX(), context.getViewUpperRight().getY()));
            } else {
                moveDirection = XYSupport.decreaseDistance(context.locate(), new XY(context.getViewUpperRight().getX(), context.getViewLowerLeft().getY()));
            }
        }
        return moveDirection;
    }

    public static XY nearestEntity(ControllerContext context, EntityType type) {
        logger.finer("searching for the nearest entities");
        XY position = context.locate();
        int minX = context.getViewLowerLeft().getX();
        int minY = context.getViewUpperRight().getY();
        int maxX = context.getViewUpperRight().getX();
        int maxY = context.getViewLowerLeft().getY();

        try {
            XY nearestEntity = new XY(100, 100);
            for (int x = minX; x < maxX; x++) {
                for (int y = minY; y < maxY; y++) {
                    if (context.getEntityAt(new XY(x, y)) == type) {
                        double distanceTo = position.distanceFrom(new XY(x, y));
                        if (distanceTo < position.distanceFrom(nearestEntity)) {
                            nearestEntity = new XY(x, y);
                        }
                    }
                }
            }
            return nearestEntity;
        } catch (OutOfViewException e) {
            logger.finer("No Entity in vector");
        }
        return null;
    }

    static boolean checkSpawnField(ControllerContext context, XY location) {
        try {
            EntityType entityType = context.getEntityAt(location);
            return entityType == EntityType.EMPTY_FIELD;
        } catch (OutOfViewException e) {
            logger.finer("No Entity in spawnfield");
        }
        return false;
    }
}
