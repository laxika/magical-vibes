package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MakeAllCreaturesUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "WAR", collectorNumber = "270")
public class JaceArcaneStrategist extends Card {

    public JaceArcaneStrategist() {
        target(TargetFilters.creatureYouControl())
                .addTrigger(Triggers.youDrawSecondCardEachTurn(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                +1,
                List.of(new DrawCardEffect(1)),
                "+1: Draw a card."
        ));

        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(MakeAllCreaturesUnblockableEffect.ownCreatures()),
                "−7: Creatures you control can't be blocked this turn."
        ));
    }
}
