package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts the card a preceding hand or library choice selected onto the battlefield: the stack entry's chosen card
 * ({@code StackEntry.getChosenObjectCard()}), if it is still in its controller's hand.
 */
public record PutChosenCardFromHandOntoBattlefieldEffect(boolean enterTapped) implements CardEffect {

    public PutChosenCardFromHandOntoBattlefieldEffect() {
        this(false);
    }
}
