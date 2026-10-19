package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Follow-up of a spellbook draft whose chosen card is neither put into hand nor exiled: the drafted
 * card is held until a later {@link PutHeldSpellbookCardsOntoBattlefieldEffect} in the same resolution
 * puts every held card onto the battlefield together ("draft a card from the spellbook twice, then put
 * those cards onto the battlefield"). The draft fills in {@code card} through
 * {@link ChosenCardAwareEffect}.
 */
public record HoldDraftedSpellbookCardEffect(Card card) implements CardEffect, ChosenCardAwareEffect {

    public HoldDraftedSpellbookCardEffect() {
        this(null);
    }

    @Override
    public CardEffect withChosenCard(Card chosen) {
        return new HoldDraftedSpellbookCardEffect(chosen);
    }
}
