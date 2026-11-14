package com.github.laxika.magicalvibes.model.rules;

import com.github.laxika.magicalvibes.model.StackEntry;

import java.util.Objects;

/**
 * A spell or ability's targets were chosen as it was put on the stack (CR 601.2c, 602.2b, 603.3d): the event "whenever
 * [a permanent or player] becomes the target of a spell or ability" and "whenever a player chooses one or more targets"
 * abilities trigger on. The players and permanents it targets become its targets together (CR 603.2c).
 *
 * @param kind      what the event is
 * @param targeting the spell or ability on the stack whose targets were chosen
 */
public record TargetEvent(Kind kind, StackEntry targeting) implements RulesEvent {

    /** What a target event is. */
    public enum Kind {
        /** Players and permanents became the targets of a spell. */
        BECAME_TARGET_OF_SPELL,
        /** Players and permanents became the targets of an activated or triggered ability. */
        BECAME_TARGET_OF_ABILITY,
        /** A player chose the spell or ability's targets: "whenever a player chooses one or more targets". */
        TARGETS_CHOSEN
    }

    public TargetEvent {
        Objects.requireNonNull(kind, "kind");
        Objects.requireNonNull(targeting, "targeting");
    }

    @Override
    public TargetEvent deepCopy() {
        return new TargetEvent(kind, new StackEntry(targeting));
    }
}
