package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEmbalmEqualToManaCostToCreatureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "MOC", collectorNumber = "58")
public class Naktamun extends Card {

    public Naktamun() {
        addEffect(EffectSlot.STATIC, new GrantEmbalmEqualToManaCostToCreatureCardsEffect());
        addEffect(EffectSlot.CHAOS_TRIGGERED,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
