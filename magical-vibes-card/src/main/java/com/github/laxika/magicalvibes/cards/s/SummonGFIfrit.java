package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAndDrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "FIN", collectorNumber = "163")
@CardRegistration(set = "FIN", collectorNumber = "369")
public class SummonGFIfrit extends Card {

    public SummonGFIfrit() {
        addEffect(EffectSlot.SAGA_CHAPTER_I,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
        addEffect(EffectSlot.SAGA_CHAPTER_II,
                new MayEffect(new DiscardAndDrawCardEffect(), "Discard a card to draw a card?"));
        addEffect(EffectSlot.SAGA_CHAPTER_III, new AwardManaEffect(ManaColor.RED));
        addEffect(EffectSlot.SAGA_CHAPTER_IV, new AwardManaEffect(ManaColor.RED));
    }
}
