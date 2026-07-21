package com.github.laxika.magicalvibes.model.effect;

/**
 * Sages of the Anima: if you would draw a card, instead reveal the top three cards of your library, put
 * all revealed creature cards into your hand, and put the rest on the bottom of your library in any order.
 */
public record SagesOfTheAnimaDrawReplacementEffect()
        implements RevealTopCardsCreaturesToHandDrawReplacementEffect {

    @Override
    public int revealCount() {
        return 3;
    }
}
