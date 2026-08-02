package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Returns the exact number of targeted cards represented by the spell's X value from the
 * controller's graveyard to the battlefield.
 *
 * <p>The target count is stored on the stack entry because it is paid X, while this effect carries
 * the card restriction. Targets are selected before the spell is put on the stack.</p>
 */
public record ReturnTargetCardsFromGraveyardToBattlefieldEffect(CardPredicate filter) implements CardEffect {
}
