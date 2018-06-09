package de.hsa.games.fatsquirrel.core;

import de.hsa.games.fatsquirrel.entities.Entity;
import de.hsa.games.fatsquirrel.entities.HandOperatedMasterSquirrel;
import de.hsa.games.fatsquirrel.entities.Wall;
import de.hsa.games.fatsquirrel.util.EntityAnnotation;
import de.hsa.games.fatsquirrel.util.XYSupport;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public class Board {

    private ObservableList<Entity> entities;
    private int entityCounter;
    private BoardConfig boardConfig;
    private FlattenedBoard flattenedBoard;
    private int width, height;

    public Board(BoardConfig boardConfig) {
        this.boardConfig = boardConfig;
        width = boardConfig.getSize().getX();
        height = boardConfig.getSize().getY();
        entities = FXCollections.observableArrayList();
        entities.addListener(new ListChangeListener<Entity>() {
            @Override
            public void onChanged(Change<? extends Entity> c) {
                while (c.next()) {
                    if (c.wasAdded())
                        entityCounter++;
                    else if (c.wasRemoved())
                        entityCounter--;
                }
            }
        });
        flattenedBoard = new FlattenedBoard(this);
        // Initiialization must be AFTER instantiation of FlattenedBoard
        initBoard();
    }

    public BoardConfig getBoardConfig() {
        return boardConfig;
    }

    public XY getSize() {
        return boardConfig.getSize();
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getEntityCounter() {
        return entityCounter;
    }

    public void insertEntity(Entity entity) {
        entities.add(entity);
    }

    public void deleteEntity(Entity entity) {
        entities.remove(entity);
    }

    public List<Entity> getEntities() {
        return entities;
    }

    public FlattenedBoard flatten() {
        return flattenedBoard;
    }

    public boolean isInBoardRange(XY pos) {
        int x = pos.getX();
        int y = pos.getY();
        return !((x < 0 || x >= getWidth()) ||
                (y < 0 || y >= getHeight()));
    }

    public ObservableList<Entity> getObservableList() {
        return entities;
    }

    private void initBoard() {
        Map<EntityType, Integer> map = getBoardConfig().getAmountByEntityType();
        XY pos;
        for (int i = 0; i < map.size(); i++) {
            EntityType iterType = EntityType.values()[i];
            for (int j = 0; j < map.get(iterType); j++) {
                insertEntity(instantiateEntity(i));
            }
        }
        // Instantiate upper & lower boundary walls
        for (int x = 0; x < getWidth(); x++) {
            insertEntity(new Wall(Entity.ID_AUTO_GENERATE, new XY(x, 0)));
            insertEntity(new Wall(Entity.ID_AUTO_GENERATE, new XY(x, getHeight() - 1)));
        }
        // Instantiate upper & lower boundary walls
        for (int y = 1; y < getHeight(); y++) {
            insertEntity(new Wall(Entity.ID_AUTO_GENERATE, new XY(0, y)));
            insertEntity(new Wall(Entity.ID_AUTO_GENERATE, new XY(getWidth() - 1, y)));
        }
        pos = XYSupport.getRandomEmptyPosition(getWidth(), getHeight(), flatten());
        insertEntity(new HandOperatedMasterSquirrel(Entity.ID_AUTO_GENERATE, 100, pos));
    }

    private Entity instantiateEntity(int i) {
        Field[] fieldList = EntityType.class.getDeclaredFields();
        Field field = fieldList[i];
        EntityAnnotation t = (EntityAnnotation) field.getDeclaredAnnotations()[0];
        Class clazz = t.entity();
        try {
            XY pos = XYSupport.getRandomEmptyPosition(getWidth(), getHeight(), flatten());
            return (Entity) clazz.getConstructor(Integer.TYPE, XY.class)
                    .newInstance(Entity.ID_AUTO_GENERATE, pos);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
