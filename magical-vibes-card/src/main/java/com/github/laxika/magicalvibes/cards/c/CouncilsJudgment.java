package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.WillOfTheCouncilEffect;

@CardRegistration(set = "VMA", collectorNumber = "20")
public class CouncilsJudgment extends Card {

    public CouncilsJudgment() {
        addEffect(EffectSlot.SPELL, new WillOfTheCouncilEffect());
    }
}
