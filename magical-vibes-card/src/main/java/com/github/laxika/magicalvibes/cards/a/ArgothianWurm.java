package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect;

@CardRegistration(set = "USG", collectorNumber = "236")
public class ArgothianWurm extends Card {

    public ArgothianWurm() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AnyPlayerMaySacrificeLandPutSourceOnTopEffect());
    }
}
