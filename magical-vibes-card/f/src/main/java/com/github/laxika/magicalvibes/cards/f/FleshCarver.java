package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenWithDyingSourcePowerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "C14", collectorNumber = "22")
public class FleshCarver extends Card {

    public FleshCarver() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{B}",
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE, 2)
                ),
                "{1}{B}, Sacrifice another creature: Put two +1/+1 counters on this creature."
        ));

        addTrigger(Triggers.dies(ObjectRelation.THIS), new CreateTokenWithDyingSourcePowerEffect(
                new CreateTokenEffect("Horror", 0, 0, CardColor.BLACK,
                        List.of(CardSubtype.HORROR), Set.of(), Set.of())
        ));
    }
}
