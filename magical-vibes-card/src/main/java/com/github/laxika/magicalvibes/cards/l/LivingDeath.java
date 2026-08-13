package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.effect.l.LivingDeathEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;

@CardRegistration(set = "TMP", collectorNumber = "142")
@CardRegistration(set = "TPR", collectorNumber = "109")
public class LivingDeath extends Card {

    public LivingDeath() {
        addEffect(EffectSlot.SPELL, new LivingDeathEffect());
    }
}
