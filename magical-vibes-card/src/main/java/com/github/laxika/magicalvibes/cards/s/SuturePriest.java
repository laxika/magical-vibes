package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "25")
public class SuturePriest extends Card {

    public SuturePriest() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        addEffect(EffectSlot.ON_OPPONENT_CREATURE_ENTERS_BATTLEFIELD,
                new MayEffect(new TargetPlayerLosesLifeEffect(1), "Have that player lose 1 life?"));
    }
}
