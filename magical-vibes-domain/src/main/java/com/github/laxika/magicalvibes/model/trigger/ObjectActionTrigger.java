package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

import java.util.Objects;

/**
 * A permanent or card takes a keyword action, or one is taken with it: "whenever a creature you control explores",
 * "when this creature exploits a creature" (CR 702.110b), "when this card becomes plotted", "whenever this creature
 * crews a Vehicle", "when you unlock this door". Build it through
 * {@link Triggers}.
 *
 * @param action what the object does or what happens to it
 * @param object which object, relative to the ability's source and controller
 */
public record ObjectActionTrigger(ObjectAction action, ObjectRelation object) implements Trigger {

    public ObjectActionTrigger {
        Objects.requireNonNull(action, "action");
        Objects.requireNonNull(object, "object");
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
        /** A saddle ability of the Mount resolves: it becomes saddled (CR 702.171a). */
        BECOMES_SADDLED,
        /** A creature stations the Spacecraft (CR 702.184a). */
        BECOMES_STATIONED,
        /**
         * One of the Room's doors becomes unlocked: it gets that half's unlocked designation, as it enters or later
         * (CR 709.5h). Which door is the ability's {@code TriggeringRoomDoorConditionalEffect}'s.
         */
        DOOR_UNLOCKED
    }
}
