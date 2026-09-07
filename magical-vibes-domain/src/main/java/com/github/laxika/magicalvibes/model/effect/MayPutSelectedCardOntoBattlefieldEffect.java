package com.github.laxika.magicalvibes.model.effect;

/** Offers the selected card, which is currently in hand, for the battlefield. */
public record MayPutSelectedCardOntoBattlefieldEffect(int manaValueAtMost) implements CardEffect {
}
