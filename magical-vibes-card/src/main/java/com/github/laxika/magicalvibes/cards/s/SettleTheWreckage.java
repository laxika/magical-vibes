package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect;

@CardRegistration(set = "XLN", collectorNumber = "34")
public class SettleTheWreckage extends Card {

    public SettleTheWreckage() {
        addEffect(EffectSlot.SPELL, new ExileTargetPlayerAttackingCreaturesAndSearchBasicLandsToBattlefieldTappedEffect());
    }
}
