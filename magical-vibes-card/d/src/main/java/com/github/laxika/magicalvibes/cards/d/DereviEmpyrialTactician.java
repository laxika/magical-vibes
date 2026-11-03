package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceFromCommandZoneOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "186")
@CardRegistration(set = "BLC", collectorNumber = "87")
@CardRegistration(set = "CMA", collectorNumber = "176")
public class DereviEmpyrialTactician extends Card {

    public DereviEmpyrialTactician() {
        target(TargetFilters.permanent()).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayEffect(new TapOrUntapTargetPermanentEffect(),
                        "You may tap or untap that permanent?"));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer()
                        .withFilter(new PermanentIsCreaturePredicate()),
                new MayEffect(new TapOrUntapTargetPermanentEffect(), "You may tap or untap that permanent?"));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{G}{W}{U}",
                List.of(new PutSourceFromCommandZoneOntoBattlefieldEffect()),
                "{1}{G}{W}{U}: Put Derevi onto the battlefield from the command zone."
        ).withActivationCondition(
                new SourceCardInCommandZone(),
                "This ability can only be activated from the command zone."));
    }
}
