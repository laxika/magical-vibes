package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;

import java.util.Objects;

/**
 * A permanent or card takes a keyword action, or one is taken with it: "whenever a creature you control explores",
 * "when this creature exploits a creature" (CR 702.110b), "when this card becomes plotted". Build it through
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
        MUTATES
    }
}
