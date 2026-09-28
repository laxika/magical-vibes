package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.condition.ControlsPermanentCount;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "LCC", collectorNumber = "43")
@CardRegistration(set = "LCC", collectorNumber = "75")
public class TheIndomitable extends Card {

    public TheIndomitable() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(null, new DrawCardEffect(1)));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(3), AnimatePermanentsEffect.crew()),
                "Crew 3"
        ));

        PermanentPredicate pirateOrVehicle = new PermanentAnyOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.PIRATE),
                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)));
        PermanentPredicate tappedPirateOrVehicle = new PermanentAllOfPredicate(List.of(
                new PermanentIsTappedPredicate(), pirateOrVehicle));
        addCastingOption(new GraveyardCast(new ControlsPermanentCount(3, tappedPirateOrVehicle)));
    }
}
