package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "FDN", collectorNumber = "90")
public class IncineratingBlast extends Card {

    public IncineratingBlast() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(6));
        addEffect(EffectSlot.SPELL, new MayEffect(
                new DiscardAndDrawCardEffect(),
                "Discard a card to draw a card?"
        ));
    }
}
