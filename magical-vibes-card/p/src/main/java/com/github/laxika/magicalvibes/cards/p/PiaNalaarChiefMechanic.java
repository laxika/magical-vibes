package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;
import java.util.Set;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect.Resource;
import com.github.laxika.magicalvibes.model.effect.EachPlayerPaysAnyManaEffect;

@CardRegistration(set = "DRC", collectorNumber = "2")
public class PiaNalaarChiefMechanic extends Card {

    public PiaNalaarChiefMechanic() {
        var artifactCreature = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));

        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer().withFilter(artifactCreature),
                TriggerOptions.ONE_OR_MORE,
                new EnergyCountersEffect(2));

        var aetherjet = new CreateTokenEffect(
                CardType.ARTIFACT, new Fixed(1), "Nalaar Aetherjet", AmountThisWay.energyPaid(),
                AmountThisWay.energyPaid(),
                null, null, List.of(CardSubtype.VEHICLE), Set.of(Keyword.FLYING), Set.of(), false, false,
                List.of(), List.of(new ActivatedAbility(
                        false,
                        null,
                        List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                        "Crew 2")), false, false, false, 0, Set.of());

        addTrigger(Triggers.endStep(ActivePlayer.YOU), SequenceEffect.of(
                EachPlayerPaysAnyManaEffect.byYou(Resource.ENERGY),
                new ConditionalEffect(new HappenedThisWay(StepKind.PAY_ENERGY), aetherjet)));
    }
}
