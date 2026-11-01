package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeCause;

import java.util.List;
import java.util.Set;

/** One factory per trigger event, for {@code Card.addTrigger}. */
public final class Triggers {

    /** A creature. */
    public static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();
    /** A nontoken creature. */
    public static final PermanentPredicate NONTOKEN_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    /** An artifact or a creature. */
    public static final PermanentPredicate ARTIFACT_OR_CREATURE = new PermanentAnyOfPredicate(List.of(
            new PermanentIsArtifactPredicate(), new PermanentIsCreaturePredicate()));
    /** A creature or a planeswalker. */
    public static final PermanentPredicate CREATURE_OR_PLANESWALKER = new PermanentAnyOfPredicate(List.of(
            new PermanentIsCreaturePredicate(), new PermanentIsPlaneswalkerPredicate()));

    private Triggers() {
    }

    /** "When [object] dies": put into a graveyard from the battlefield (CR 700.4). */
    public static ZoneChangeTrigger dies(ObjectRelation relation) {
        return dies(null, relation);
    }

    /**
     * "Whenever [a filter object] dies": {@code dies(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL)} for
     * "whenever another creature you control dies". The object is judged as it last existed on the battlefield
     * (CR 603.10a).
     */
    public static ZoneChangeTrigger dies(PermanentPredicate filter, ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, filter, Set.of(Zone.BATTLEFIELD), Zone.GRAVEYARD, Set.of());
    }

    /** "Whenever [a creature] dies", for {@code relation}: {@code dies(CREATURE, relation)}. */
    public static ZoneChangeTrigger creatureDies(ObjectRelation relation) {
        return dies(CREATURE, relation);
    }

    /** "Whenever [a creature that also matches {@code filter}] dies": "whenever this or another Zombie dies". */
    public static ZoneChangeTrigger creatureDies(PermanentPredicate filter, ObjectRelation relation) {
        return dies(new PermanentAllOfPredicate(List.of(CREATURE, filter)), relation);
    }

    /** "Whenever [a nontoken creature] dies", for {@code relation}. */
    public static ZoneChangeTrigger nontokenCreatureDies(ObjectRelation relation) {
        return dies(NONTOKEN_CREATURE, relation);
    }

    /** "Whenever [a nontoken creature that also matches {@code filter}] dies". */
    public static ZoneChangeTrigger nontokenCreatureDies(PermanentPredicate filter, ObjectRelation relation) {
        return dies(new PermanentAllOfPredicate(List.of(NONTOKEN_CREATURE, filter)), relation);
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

    /**
     * "When this spell is put into a graveyard during its resolution", as haunt reads on an instant or sorcery
     * (CR 702.55a). A spell countered, or one that doesn't resolve because its targets became illegal, isn't.
     */
    public static ZoneChangeTrigger putIntoGraveyardWhileResolving(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.STACK), Zone.GRAVEYARD, Set.of(ZoneChangeCause.RESOLUTION));
    }

    /** "When [object] is milled": put into a graveyard from a library by a mill. */
    public static ZoneChangeTrigger milled(ObjectRelation relation) {
        return new ZoneChangeTrigger(relation, Set.of(Zone.LIBRARY), Zone.GRAVEYARD, Set.of(ZoneChangeCause.MILL));
    }
}
