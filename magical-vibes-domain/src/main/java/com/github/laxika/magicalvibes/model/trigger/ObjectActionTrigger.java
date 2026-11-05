package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * A permanent or card takes a keyword action, or one is taken with it: "whenever a creature you control explores",
 * "when this creature exploits a creature" (CR 702.110b), "when this card becomes plotted", "whenever this creature
 * crews a Vehicle", "when you unlock this door". Build it through
 * {@link Triggers}.
 *
 * @param action what the object does or what happens to it
 * @param object which object, relative to the ability's source and controller
 * @param filter what the card the action is taken with must be, or {@code null} for any card: the explored card
 *               ("whenever a creature you control explores a land card" is
 *               {@code Triggers.creatureYouControlExplores().withFilter(land)}) or the exploited creature. Only
 *               {@link ObjectAction#EXPLORES} and {@link ObjectAction#EXPLOITS} take one
 * @param door   which door of the Room the ability is on, 0 for the left one and 1 for the right one, for
 *               {@link ObjectAction#DOOR_UNLOCKED} ("when you unlock this door", CR 709.5h:
 *               {@code Triggers.doorUnlocked(1)}); {@code null} for every other action and for the trigger the
 *               collector watches. The explore and exploit collectors judge the filter from the ability
 *               ({@code ActionAbilityReader}), and the door collector the door ({@code MiscAbilityReader}), so
 *               {@link #watchKey()} drops them
 */
public record ObjectActionTrigger(ObjectAction action, ObjectRelation object, CardPredicate filter, Integer door)
        implements Trigger {

    public ObjectActionTrigger {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(object, "object");
        if (filter != null && action != ObjectAction.EXPLORES && action != ObjectAction.EXPLOITS) {
            throw new IllegalArgumentException("Only an explore or exploit trigger takes a card filter: " + action);
        }
        if (door != null && (action != ObjectAction.DOOR_UNLOCKED || door < 0 || door > 1)) {
            throw new IllegalArgumentException("Only a door-unlocked trigger names a door, 0 or 1: " + action
                    + " " + door);
        }
    }

    /** A trigger on any object of {@code action} that relates to the source as {@code object} says. */
    public ObjectActionTrigger(ObjectAction action, ObjectRelation object) {
        this(action, object, null, null);
    }

    /**
     * This trigger narrowed to an action taken with a card that matches {@code filter}, as {@link #filter} says:
     * "whenever a creature you control exploits a non-Human creature".
     */
    public ObjectActionTrigger withFilter(CardPredicate filter) {
        Objects.requireNonNull(filter, "filter");
        if (this.filter != null) {
            throw new IllegalStateException("This trigger already has a filter: " + this);
        }
        return new ObjectActionTrigger(action, object, filter, door);
    }

    /**
     * The trigger the collectors read this one's abilities by: without its {@link #filter} and {@link #door}, and,
     * for "becomes crewed / saddled for the first time each turn", the "becomes crewed / saddled" trigger, whose
     * collector judges the first time from the ability's trigger ({@code MiscAbilityReader}).
     */
    @Override
    public Trigger watchKey() {
        ObjectAction watched = switch (action) {
            case BECOMES_CREWED_FOR_FIRST_TIME_EACH_TURN -> ObjectAction.BECOMES_CREWED;
            case BECOMES_SADDLED_FOR_FIRST_TIME_EACH_TURN -> ObjectAction.BECOMES_SADDLED;
            default -> action;
        };
        return watched == action && filter == null && door == null ? this : new ObjectActionTrigger(watched, object);
    }

    /** A card becomes plotted in exile (CR 702.170a), so its "becomes plotted" ability functions from there. */
    @Override
    public AbilityZones defaultZones() {
        return action == ObjectAction.BECOMES_PLOTTED ? AbilityZones.EXILE : AbilityZones.BATTLEFIELD;
    }

    /** What an object does, or what happens to it. */
    public enum ObjectAction {
        /** The creature fights. */
        FIGHTS,
        /** The creature explores. */
        EXPLORES,
        /** The creature connives. */
        CONNIVES,
        /** The creature exploits a creature (CR 702.110b). */
        EXPLOITS,
        /** The creature exploits a nontoken creature. */
        EXPLOITS_NONTOKEN_CREATURE,
        /** A creature is championed with this object: exiled by its champion ability. */
        CHAMPIONS,
        /** The card becomes plotted (CR 702.170a). */
        BECOMES_PLOTTED,
        /** The creature mutates. */
        MUTATES,
        /** The creature is tapped to pay a Vehicle's crew cost (CR 702.122a). */
        CREWS_VEHICLE,
        /**
         * The creature is tapped to pay a Mount's saddle cost (CR 702.171a) or a Vehicle's crew cost during its
         * controller's main phase.
         */
        SADDLES_OR_CREWS_DURING_MAIN_PHASE,
        /** A crew ability of the Vehicle resolves (CR 702.122e). */
        BECOMES_CREWED,
        /**
         * The Vehicle becomes crewed for the first time this turn. The collector judges it as the "becomes crewed"
         * collector's once-each-turn mark on the Vehicle, as the cards wrote it.
         */
        BECOMES_CREWED_FOR_FIRST_TIME_EACH_TURN,
        /** A saddle ability of the Mount resolves: it becomes saddled (CR 702.171a). */
        BECOMES_SADDLED,
        /**
         * The Mount becomes saddled for the first time this turn. The collector judges it as the "becomes saddled"
         * collector's once-each-turn mark on the Mount, as the cards wrote it.
         */
        BECOMES_SADDLED_FOR_FIRST_TIME_EACH_TURN,
        /** A creature stations the Spacecraft (CR 702.184a). */
        BECOMES_STATIONED,
        /**
         * One of the Room's doors becomes unlocked: it gets that half's unlocked designation, as it enters or later
         * (CR 709.5h). Which door is the trigger's {@code door}.
         */
        DOOR_UNLOCKED
    }
}
