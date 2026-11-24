package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.DoubleTargetCreaturePowerEffect;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "SPE", collectorNumber = "13")
public class DoubleTrouble extends Card {

    public DoubleTrouble() {
        addSpellEffect(DoubleTargetCreaturePowerEffect.ofCreaturesYouControl(null));
    }
}
