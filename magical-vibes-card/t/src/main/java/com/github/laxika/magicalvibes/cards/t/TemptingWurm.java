package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;

@CardRegistration(set = "ONS", collectorNumber = "291")
public class TemptingWurm extends Card {

    public TemptingWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                EachPlayerMayPutCardFromHandToBattlefieldEffect.temptingWurm());
    }
}
