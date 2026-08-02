package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent reveals their hand. That player exiles a card from it, then you exile a card
 * from it. Repeat this process until all cards in that hand have been exiled. That player returns
 * the cards they exiled this way to their hand and puts the rest into their graveyard."
 * (Struggle for Sanity.)
 *
 * <p>Resolution alternates a card pick between the targeted player (who starts) and the spell's
 * controller until the hand is empty; the cards the target picked go back to their hand and the
 * cards the controller picked go to their graveyard. The alternation is carried by
 * {@link com.github.laxika.magicalvibes.model.PendingInteraction.AlternatingHandExileChoice}.
 */
public record AlternatingHandExileEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PLAYER);
    }
}
