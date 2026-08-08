package com.github.laxika.magicalvibes.model.effect;

/**
 * Tomorrow, Azami's Familiar: if you would draw a card, look at the top three cards of your library
 * instead. Put one of those cards into your hand and the rest on the bottom of your library in any order.
 */
public record TomorrowAzamisFamiliarDrawReplacementEffect()
        implements LookAtTopCardsChooseOneToHandDrawReplacementEffect {

    @Override
    public int lookCount() {
        return 3;
    }
}
