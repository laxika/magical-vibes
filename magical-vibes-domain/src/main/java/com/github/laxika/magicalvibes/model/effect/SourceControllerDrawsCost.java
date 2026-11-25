package com.github.laxika.magicalvibes.model.effect;

/**
 * "Has you draw {@code count} card(s)" as a cost another player pays as an effect resolves ("create a 3/1 red Elemental
 * creature token unless any opponent has you draw a card": Development). The payer chooses it; the controller of the
 * effect's source draws (CR 118.12a). Like {@link DamageFromSourceCost}, it is always payable: the payer pays it by
 * having the source's side act.
 *
 * <p>Paid together ({@code UnlessPaysEffect.payingTogether()}), the controller draws {@code count} cards once for the
 * round however many players chose it: the one card that answers "has you draw a card" this time (Development's
 * rulings: "you'll wind up with three Elemental tokens, two tokens and a card, one token and two cards, or three cards";
 * "a different opponent may let you draw a card each time").
 *
 * @param count the cards the source's controller draws
 */
public record SourceControllerDrawsCost(int count) implements CostEffect {

    public SourceControllerDrawsCost {
        if (count < 1) {
            throw new IllegalArgumentException("A draw cost draws at least one card");
        }
    }
}
