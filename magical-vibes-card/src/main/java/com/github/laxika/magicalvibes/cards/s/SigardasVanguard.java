package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect;

@CardRegistration(set = "MIC", collectorNumber = "8")
@CardRegistration(set = "MIC", collectorNumber = "46")
public class SigardasVanguard extends Card {

    public SigardasVanguard() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect());
        addEffect(EffectSlot.ON_ATTACK,
                new ChooseCreaturesWithDifferentPowersGrantDoubleStrikeEffect());
    }
}
