package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;

/**
 * Which of the old creature-death slot collectors puts a "whenever [an object] dies" ability on the stack, now that
 * the trigger pipeline decides whether it triggers. Each collector bound its own event data and chose its own way to
 * the stack, and step 14 and 15 of effect-slot-refactor.md replace these with trigger options and effects that read
 * the event. The trigger's relation, its filter and the zone the ability functions from pick one.
 */
public enum CreatureDeathWatch {
    /** "A creature you control" from the battlefield: one stack object for the ability's plain effects. */
    ALLY,
    /** "A creature": each effect through its handler. */
    ANY,
    /** "A creature an opponent controls". */
    OPPONENT,
    /** "A nontoken creature you control". */
    ALLY_NONTOKEN,
    /** "A nontoken creature". */
    ANY_NONTOKEN,
    /** "A nontoken creature enchanted player controls", on a Curse. */
    ENCHANTED_PLAYER_NONTOKEN,
    /** "A creature or planeswalker you control". */
    ALLY_CREATURE_OR_PLANESWALKER,
    /** "One or more artifacts and/or creatures you control". */
    ALLY_ARTIFACT_OR_CREATURE,
    /** "Equipped creature". */
    EQUIPPED,
    /** "A creature dealt damage by this creature" or "by equipped creature" this turn. */
    DAMAGED,
    /** "The creature it haunts", from exile (CR 702.55c). */
    HAUNTED,
    /** "A creature you control", from the graveyard. */
    GRAVEYARD_ALLY,
    /** "A creature an opponent controls", from the graveyard. */
    GRAVEYARD_OPPONENT;

    /**
     * The collector of a dies {@code trigger} whose relation isn't {@code THIS}, for an ability functioning in
     * {@code zone}.
     */
    public static CreatureDeathWatch of(ZoneChangeTrigger trigger, Zone zone) {
        ObjectRelation relation = trigger.relation();
        switch (relation) {
            case HAUNTED:
                return HAUNTED;
            case EQUIPPED:
                return EQUIPPED;
            case ENCHANTED_PLAYER_CONTROLS:
                return ENCHANTED_PLAYER_NONTOKEN;
            case DAMAGED_BY_THIS:
            case DAMAGED_BY_ATTACHED:
                return DAMAGED;
            default:
                break;
        }
        if (zone == Zone.GRAVEYARD) {
            return relation == ObjectRelation.OPPONENT_CONTROLS ? GRAVEYARD_OPPONENT : GRAVEYARD_ALLY;
        }
        if (relation == ObjectRelation.OPPONENT_CONTROLS) {
            // "A nontoken creature an opponent controls" too: the trigger's filter decides which creatures count.
            return OPPONENT;
        }
        PermanentPredicate base = base(trigger.filter());
        if (Triggers.NONTOKEN_CREATURE.equals(base)) {
            return relation.youControl() ? ALLY_NONTOKEN : ANY_NONTOKEN;
        }
        if (Triggers.CREATURE_OR_PLANESWALKER.equals(base)) {
            return ALLY_CREATURE_OR_PLANESWALKER;
        }
        if (Triggers.ARTIFACT_OR_CREATURE.equals(base)) {
            return ALLY_ARTIFACT_OR_CREATURE;
        }
        return relation.youControl() ? ALLY : ANY;
    }

    /**
     * The kind of object a filter names: the filter itself, or the first part of one that narrows a kind down
     * ({@code AllOf(NONTOKEN_CREATURE, Zombie)} for "another nontoken Zombie").
     */
    private static PermanentPredicate base(PermanentPredicate filter) {
        if (filter instanceof PermanentAllOfPredicate allOf && !filter.equals(Triggers.NONTOKEN_CREATURE)
                && !allOf.predicates().isEmpty()) {
            return allOf.predicates().getFirst();
        }
        return filter;
    }
}
