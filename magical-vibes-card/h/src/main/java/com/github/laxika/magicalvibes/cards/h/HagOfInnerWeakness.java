package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantDuration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HBG", collectorNumber = "H20")
public class HagOfInnerWeakness extends Card {

    public HagOfInnerWeakness() {
        // At the beginning of your upkeep, target creature an opponent controls gets -2/-1 until your next turn.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new BoostTargetCreatureEffect(-2, -1, GrantDuration.UNTIL_YOUR_NEXT_TURN));
    }
}
