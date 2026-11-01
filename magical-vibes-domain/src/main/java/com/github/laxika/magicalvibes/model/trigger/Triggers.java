package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;

import java.util.Set;

/** One factory per trigger event, for {@code Card.addTrigger}. */
public final class Triggers {

    private Triggers() {
    }

    /** "When [object] dies": put into a graveyard from the battlefield (CR 700.4). */
    public static ZoneChangeTrigger dies(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.BATTLEFIELD), Zone.GRAVEYARD, Set.of());
    }

    /**
     * "When you sacrifice [object]": its sacrifice moves it from the battlefield (CR 701.21a), wherever a
     * replacement effect sends it.
     */
    public static ZoneChangeTrigger sacrificed(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.BATTLEFIELD), null, Set.of(ZoneChangeCause.SACRIFICE));
    }

    /** "When [object] leaves the battlefield" (CR 603.6c). */
    public static ZoneChangeTrigger leavesBattlefield(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.BATTLEFIELD), null, Set.of());
    }

    /** "When [object] is put into a graveyard from [zone]". */
    public static ZoneChangeTrigger putIntoGraveyardFrom(Zone from, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(from), Zone.GRAVEYARD, Set.of());
    }

    /**
     * "When [object] is put into a graveyard from anywhere". It is never a leaves-the-battlefield ability, even
     * for a move from the battlefield (CR 603.6c), so the source's own move triggers it from the graveyard.
     */
    public static ZoneChangeTrigger putIntoGraveyardFromAnywhere(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(), Zone.GRAVEYARD, Set.of());
    }

    /** "When [object] is milled": put into a graveyard from a library by a mill. */
    public static ZoneChangeTrigger milled(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.LIBRARY), Zone.GRAVEYARD, Set.of(ZoneChangeCause.MILL));
    }
}
