package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "ECL", collectorNumber = "162")
public class Tweeze extends Card {

    public Tweeze() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
    }
}
