package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureCantBlockThisTurnEffect;

@CardRegistration(set = "ISD", collectorNumber = "135")
public class CrosswayVampire extends Card {

    public CrosswayVampire() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetCreatureCantBlockThisTurnEffect());
    }
}
