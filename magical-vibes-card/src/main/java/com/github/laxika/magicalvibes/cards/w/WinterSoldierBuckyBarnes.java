package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;

@CardRegistration(set = "MSC", collectorNumber = "527")
public class WinterSoldierBuckyBarnes extends Card {

    public WinterSoldierBuckyBarnes() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
    }
}
