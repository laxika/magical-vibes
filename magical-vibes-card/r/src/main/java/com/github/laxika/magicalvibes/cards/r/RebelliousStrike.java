package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "TDM", collectorNumber = "20")
public class RebelliousStrike extends Card {

    public RebelliousStrike() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 0));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
