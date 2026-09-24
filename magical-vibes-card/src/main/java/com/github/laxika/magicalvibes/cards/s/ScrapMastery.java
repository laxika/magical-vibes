package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.effect.l.LivingDeathEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;

@CardRegistration(set = "C14", collectorNumber = "38")
public class ScrapMastery extends Card {

    public ScrapMastery() {
        addEffect(EffectSlot.SPELL, new LivingDeathEffect(CardType.ARTIFACT));
    }
}
