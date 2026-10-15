package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect;

@CardRegistration(set = "MIC", collectorNumber = "29")
@CardRegistration(set = "MIC", collectorNumber = "67")
public class SigardianZealot extends Card {

    public SigardianZealot() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new ChooseCreaturesWithDifferentPowersBoostAndGrantVigilanceEffect());
    }
}
