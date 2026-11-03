package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AllOf;
import com.github.laxika.magicalvibes.model.condition.AttackedPlayerPoisoned;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.GivePoisonCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PoisonRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONC", collectorNumber = "9")
@CardRegistration(set = "ONC", collectorNumber = "47")
public class NornsDecree extends Card {

    public NornsDecree() {
        addTrigger(Triggers.creatureDealsCombatDamageToYou()
                        .withFilter(new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())),
                TriggerOptions.ONE_OR_MORE,
                new GivePoisonCountersEffect(1, PoisonRecipient.TARGET_PERMANENT_CONTROLLER));
        addTrigger(Triggers.playerAttacks(),
                TriggerOptions.DEFAULT.withInterveningIf(new AllOf(List.of(new AttackedPlayerPoisoned()))),
                new DrawCardForTriggeringPlayerEffect());
    }
}
