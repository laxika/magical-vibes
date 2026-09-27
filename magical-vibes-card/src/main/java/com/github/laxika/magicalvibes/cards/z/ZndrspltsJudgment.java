package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ZndrsplatsJudgmentEffect;

@CardRegistration(set = "NCC", collectorNumber = "240")
public class ZndrspltsJudgment extends Card {

    public ZndrspltsJudgment() {
        addEffect(EffectSlot.SPELL, new ZndrsplatsJudgmentEffect());
    }
}
