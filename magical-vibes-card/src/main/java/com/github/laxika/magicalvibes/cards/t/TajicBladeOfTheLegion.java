package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.MinimumAttackers;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

@CardRegistration(set = "DGM", collectorNumber = "107")
public class TajicBladeOfTheLegion extends Card {

    public TajicBladeOfTheLegion() {
        addEffect(EffectSlot.ON_ATTACK, new ConditionalEffect(
                new MinimumAttackers(3),
                new BoostSelfEffect(5, 5)));
    }
}
