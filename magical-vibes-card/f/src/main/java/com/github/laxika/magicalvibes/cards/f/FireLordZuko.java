package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AwardManaUntilEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "TLA", collectorNumber = "221")
public class FireLordZuko extends Card {

    public FireLordZuko() {
        addTrigger(Triggers.attacks(),
                new AwardManaUntilEndOfCombatEffect(ManaColor.RED, new SourcePower()));

        PutCounterOnEachControlledPermanentEffect counterEffect =
                new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 1, new PermanentIsCreaturePredicate());
        addTrigger(Triggers.youCastSpell(),
                new SpellCastTriggerEffect(null, List.of(counterEffect),
                        new StackEntryCastFromZonePredicate(Zone.EXILE)));
        addTrigger(Triggers.entersFrom(Zone.EXILE, null, ObjectRelation.YOU_CONTROL), counterEffect);
    }
}
