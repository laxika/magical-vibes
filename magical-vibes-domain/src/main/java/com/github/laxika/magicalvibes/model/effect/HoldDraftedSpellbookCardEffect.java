package com.github.laxika.magicalvibes.model.effect;

/**
 * Follow-up of a spellbook draft whose chosen card is neither put into hand nor exiled: the drafted
 * card (the stack entry's chosen card, {@code StackEntry.getChosenObjectCard()}) is held until a later
 * {@link PutHeldSpellbookCardsOntoBattlefieldEffect} in the same resolution puts every held card onto the
 * battlefield together ("draft a card from the spellbook twice, then put those cards onto the battlefield").
 */
public record HoldDraftedSpellbookCardEffect() implements CardEffect {
}
