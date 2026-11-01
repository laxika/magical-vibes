package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourcePowerAtLeast;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterOneShotCreatureSpellPerpetualPowerBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "51")
public class DragonbornImmolator extends Card {

    public DragonbornImmolator() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new BoostSelfEffect(1, 0)),
                "{2}{R}: This creature gets +1/+0 until end of turn."
        ));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                new ConditionalEffect(new SourcePowerAtLeast(1),
                        new RegisterOneShotCreatureSpellPerpetualPowerBoostEffect()));
    }
}
