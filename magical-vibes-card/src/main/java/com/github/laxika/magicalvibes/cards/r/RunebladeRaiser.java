package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToBattlefieldEffect;

@CardRegistration(set = "YTDM", collectorNumber = "8")
public class RunebladeRaiser extends Card {

    public RunebladeRaiser() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_DEATH,
                new ReturnSourceCardFromGraveyardToBattlefieldEffect(false, false, true));
    }
}
