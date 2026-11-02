package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MSH", collectorNumber = "201")
public class AntManColonyCommander extends Card {

    public AntManColonyCommander() {
        target(TargetFilters.creature()).addTrigger(Triggers.attacks(),
                new MayPayManaEffect(
                        "{1}",
                        new QueueReflexiveAbilityEffect(new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 1)),
                        "Pay {1} to put a +1/+1 counter on target creature?"));

        addTrigger(Triggers.youPutPlusOneCountersOnCreatureYouControlOnceEachTurn(),
                new OncePerTurnTriggerEffect(insectToken()));
        addTrigger(Triggers.youPutPlusOneCountersOnAnotherCreature(),
                new OncePerTurnTriggerEffect(insectToken()));
    }

    private static CreateTokenEffect insectToken() {
        return new CreateTokenEffect(
                "Insect", 1, 1, CardColor.GREEN, List.of(CardSubtype.INSECT), Set.of(), Set.of());
    }
}
