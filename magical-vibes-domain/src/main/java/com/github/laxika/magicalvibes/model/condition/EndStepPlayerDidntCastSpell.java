package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * "If that player didn't cast a [type] spell this turn" on an end-step ability: the player whose end step it is
 * (the stack entry's non-targeting {@code targetId}, and the reader's context target as the ability triggers) cast
 * no spell this turn, or none of {@code spellType} when it isn't {@code null}. A countered spell still counts as
 * cast. Used by Impatience (any spell) and Predatory Advantage (a creature spell).
 */
public record EndStepPlayerDidntCastSpell(CardType spellType) implements Condition {

    @Override
    public String conditionName() {
        return spellType == null ? "didn't cast a spell"
                : "didn't cast a " + spellType.name().toLowerCase() + " spell";
    }

    @Override
    public String conditionNotMetReason() {
        return spellType == null ? "that player cast a spell this turn"
                : "that player cast a " + spellType.name().toLowerCase() + " spell this turn";
    }
}
