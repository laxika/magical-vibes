package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;

public class MortalCombat extends Card {

    public MortalCombat() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new WinGameIfCreaturesInGraveyardEffect(20));
    }
}
