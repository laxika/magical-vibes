package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCreaturesOfChosenTypeFromGraveyardEffect;

@CardRegistration(set = "ECL", collectorNumber = "91")
public class BloodlineBidding extends Card {

    public BloodlineBidding() {
        addEffect(EffectSlot.SPELL, new ReturnCreaturesOfChosenTypeFromGraveyardEffect());
    }
}
