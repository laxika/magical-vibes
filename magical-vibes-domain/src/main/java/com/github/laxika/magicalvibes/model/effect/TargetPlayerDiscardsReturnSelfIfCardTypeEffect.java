package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

/**
 * Forces the targeted player to discard cards. If a discarded card matches the required type,
 * the source spell is returned from its owner's graveyard to their hand.
 * <p>
 * Example: Psychic Miasma — "Target player discards a card. If a land card is discarded this way,
 * return Psychic Miasma to its owner's hand."
 *
 * @param amount       number of cards to discard
 * @param returnIfType the card type that triggers returning the source spell to hand
 */
public record TargetPlayerDiscardsReturnSelfIfCardTypeEffect(int amount, CardType returnIfType) implements CardEffect {

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
