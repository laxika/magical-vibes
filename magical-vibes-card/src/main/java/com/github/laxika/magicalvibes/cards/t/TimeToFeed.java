package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.FightTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "THS", collectorNumber = "181")
public class TimeToFeed extends Card {

    public TimeToFeed() {
        // When that creature dies this turn, you gain 3 life.
        target(TargetFilters.creatureAnOpponentControls())
                .addEffect(EffectSlot.SPELL,
                        new ResolveEffectOnTargetDeathThisTurnEffect(new GainLifeEffect(3)));

        // Target creature you control fights that creature.
        target(TargetFilters.creatureYouControl())
                .addEffect(EffectSlot.SPELL, new FightTargetsEffect());
    }
}
