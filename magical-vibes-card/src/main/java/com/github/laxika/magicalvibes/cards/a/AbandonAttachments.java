package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "TLA", collectorNumber = "205")
public class AbandonAttachments extends Card {

    public AbandonAttachments() {
        addEffect(EffectSlot.SPELL, new MayEffect(
                new DiscardCardThenEffect(null, new DrawCardEffect(2), "a card"),
                "Discard a card to draw two cards?"));
    }
}
