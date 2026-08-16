package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "M21", collectorNumber = "130")
public class BattleRattleShaman extends Card {

    public BattleRattleShaman() {
        // At the beginning of combat on your turn, you may have target creature get +2/+0 until
        // end of turn.
        target(TargetFilters.creature()).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayEffect(
                        new BoostTargetCreatureEffect(2, 0),
                        "Have target creature get +2/+0 until end of turn?"
                ));
    }
}
