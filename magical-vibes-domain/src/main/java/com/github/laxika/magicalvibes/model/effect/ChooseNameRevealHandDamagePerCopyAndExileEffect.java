package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.List;

/**
 * Choose a card name (excluding cards of the given types), then the target player reveals their
 * hand. The source deals {@code damagePerCard} damage to that player for each card with the chosen
 * name revealed from their hand this way. Then search that player's graveyard, hand, and library
 * for <b>all</b> cards with that name and exile them (no choice — mandatory), and that player
 * shuffles their library.
 * <p>
 * Differs from {@link ChooseCardNameAndExileFromZonesEffect} (Memoricide): that effect exiles
 * "any number" (a player choice) and deals no damage; this one reveals the hand, deals damage per
 * copy, and exiles every copy automatically.
 * <p>
 * Used by: Thought Hemorrhage ({@code damagePerCard = 3}, excluding lands).
 */
public record ChooseNameRevealHandDamagePerCopyAndExileEffect(List<CardType> excludedTypes, int damagePerCard)
        implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
