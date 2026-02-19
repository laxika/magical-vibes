package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "160")
public class MortalCombat extends Card {

    public MortalCombat() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new WinGameIfCreaturesInGraveyardEffect(20));
    }
}
