package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.ControllerTurn;
import com.github.laxika.magicalvibes.model.effect.AttachSourceEquipmentToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeAttackedWhileAttachedToCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "231")
public class TheAetherspark extends Card {

    public TheAetherspark() {
        addStaticEffect(new CantBeAttackedWhileAttachedToCreatureEffect());
        addTrigger(Triggers.equippedCreatureDealsCombatDamage(),
                TriggerOptions.DEFAULT.withInterveningIf(new ControllerTurn()),
                new PutCountersOnSelfEffect(CounterType.LOYALTY, new EventValue()));

        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(
                        new AttachSourceEquipmentToTargetCreatureEffect(),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)
                ),
                "+1: Attach The Aetherspark to up to one target creature you control. Put a +1/+1 counter on that creature.",
                TargetFilters.creatureYouControl(),
                +1, null, null, List.of(), 0, 1
        ));

        addActivatedAbility(new ActivatedAbility(
                -5,
                List.of(new DrawCardEffect(2)),
                "−5: Draw two cards."
        ));

        addActivatedAbility(new ActivatedAbility(
                -10,
                List.of(new AwardAnyColorManaEffect(10)),
                "−10: Add ten mana of any one color."
        ));
    }
}
