package com.github.laxika.magicalvibes.model.effect;

/**
 * Exile cards from the top of your library until you exile a nonland card, then put that card
 * into your hand. If the card's mana value is greater than or equal to the threshold, repeat
 * this process. Source deals {@code damagePerCard} damage to you for each card put into your
 * hand this way.
 *
 * <p>Used by Demonlord Belzenlok (threshold 4, damagePerCard 1).
 *
 * @param manaValueThreshold if the nonland card's mana value is at least this, repeat the process
 * @param damagePerCard      damage dealt to the controller for each card put into hand
 */
public record ExileUntilNonlandToHandRepeatIfHighMVEffect(
        int manaValueThreshold,
        int damagePerCard
) implements CardEffect {
}
