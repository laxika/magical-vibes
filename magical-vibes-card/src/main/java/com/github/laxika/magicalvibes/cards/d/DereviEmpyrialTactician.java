package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceCardInCommandZone;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceFromCommandZoneOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "C13", collectorNumber = "186")
public class DereviEmpyrialTactician extends Card {

    public DereviEmpyrialTactician() {
        target(TargetFilters.permanent()).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(new TapOrUntapTargetPermanentEffect(),
                        "You may tap or untap that permanent?"));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentIsCreaturePredicate(),
                        new MayEffect(new TapOrUntapTargetPermanentEffect(),
                                "You may tap or untap that permanent?")));

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
