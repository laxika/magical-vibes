package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * Grants the controller a turn-scoped permission to cast matching spells from the graveyard of
 * the player dealt combat damage by the source creature.
 */
public record AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect(
        CardPredicate filter,
        boolean singleUse,
        boolean anyManaType
) implements CombatDamageTriggerContextEffect {

    public AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect(CardPredicate filter,
                                                                          boolean anyManaType) {
        this(filter, true, anyManaType);
    }

    public AllowCastMatchingCardsFromDamagedPlayerGraveyardThisTurnEffect {
        Objects.requireNonNull(filter, "filter");
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
