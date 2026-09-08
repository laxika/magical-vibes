package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "IKO", collectorNumber = "112")
public class Cloudpiercer extends Card {

    public Cloudpiercer() {
        addEffect(EffectSlot.ON_SELF_MUTATES,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
