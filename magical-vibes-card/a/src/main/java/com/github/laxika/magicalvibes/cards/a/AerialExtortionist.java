package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayWithOpponentTaxEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "11")
@CardRegistration(set = "NCC", collectorNumber = "112")
@CardRegistration(set = "MKC", collectorNumber = "54")
public class AerialExtortionist extends Card {

    public AerialExtortionist() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentMayPlayWithOpponentTaxEffect(0))
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new ExileTargetPermanentMayPlayWithOpponentTaxEffect(0));

        addTrigger(Triggers.opponentCastsSpell()
                        .withStackEntryFilter(new StackEntryNotPredicate(new StackEntryCastFromZonePredicate(Zone.HAND))),
                new DrawCardEffect());
    }
}
