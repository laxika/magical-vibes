package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DiscardThenReturnFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;

@CardRegistration(set = "6ED", collectorNumber = "92")
public class Recall extends Card {

    public Recall() {
        // Discard X cards, then return a card from your graveyard to your hand for each card
        // discarded this way.
        addEffect(EffectSlot.SPELL, new DiscardThenReturnFromGraveyardToHandEffect(new XValue()));

        // Exile Recall.
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
