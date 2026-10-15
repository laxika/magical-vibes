package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfEnergyToCreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "DRC", collectorNumber = "2")
public class PiaNalaarChiefMechanic extends Card {

    public PiaNalaarChiefMechanic() {
        var artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(artifactCreature, new EnergyCountersEffect(2), false, true));

        var aetherjet = new CreateTokenEffect(
                CardType.ARTIFACT, new EventValue(), "Nalaar Aetherjet", new EventValue(), new EventValue(),
                null, null, List.of(CardSubtype.VEHICLE), Set.of(Keyword.FLYING), Set.of(), false, false,
                Map.of(), List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                        "Crew 2")), false, false, false, 0, Set.of());

        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new PayAnyAmountOfEnergyToCreateTokenEffect(aetherjet));
    }
}
