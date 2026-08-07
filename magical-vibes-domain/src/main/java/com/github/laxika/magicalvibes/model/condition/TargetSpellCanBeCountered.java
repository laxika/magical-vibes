package com.github.laxika.magicalvibes.model.condition;

/**
 * The spell this effect targets is still on the stack and can actually be countered by the
 * source spell — it is not uncounterable and its controller has no turn-duration protection
 * from being countered by spells of the source's color.
 *
 * <p>Gates the rider half of a "counter target spell ... [do something with] the spell countered
 * this way" card (Psychic Rebuttal), so the rider is skipped when the counter itself does
 * nothing.</p>
 */
public record TargetSpellCanBeCountered() implements Condition {

    @Override
    public String conditionName() {
        return "countered this way";
    }

    @Override
    public String conditionNotMetReason() {
        return "the target spell wasn't countered";
    }
}
