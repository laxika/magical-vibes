package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEqualToLifeLostThisTurn;

@CardRegistration(set = "SHM", collectorNumber = "81")
public class WoundReflection extends Card {

    public WoundReflection() {
        // At the beginning of each end step, each opponent loses life equal to the life they lost
        // this turn. (Damage causes loss of life.)
        addEffect(EffectSlot.END_STEP_TRIGGERED, new EachOpponentLosesLifeEqualToLifeLostThisTurn());
    }
}
