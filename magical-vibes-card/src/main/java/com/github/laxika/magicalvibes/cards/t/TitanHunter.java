package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEndStepPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "C20", collectorNumber = "48")
public class TitanHunter extends Card {

    public TitanHunter() {
        // At the beginning of each player's end step, if no creatures died this turn,
        // this creature deals 4 damage to that player.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NotCondition(new Morbid()),
                new DealDamageToEndStepPlayerEffect(new Fixed(4))));

        // {1}{B}, Sacrifice a creature: You gain 4 life.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(new SacrificeCreatureCost(), new GainLifeEffect(4)),
                "{1}{B}, Sacrifice a creature: You gain 4 life."
        ));
    }
}
