package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CrewCost;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MOC", collectorNumber = "51")
public class Ghirapur extends Card {

    public Ghirapur() {
        PermanentAllOfPredicate noncreatureNonVehicleArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))));
        ActivatedAbility crewTwo = new ActivatedAbility(
                false,
                null,
                List.of(new CrewCost(2), AnimatePermanentsEffect.crew()),
                "Crew 2");
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new GrantActivatedAbilityEffect(
                        crewTwo,
                        GrantScope.OWN_PERMANENTS,
                        noncreatureNonVehicleArtifact,
                        EffectDuration.UNTIL_END_OF_TURN),
                new AnimatePermanentsEffect(
                        new Fixed(5), new Fixed(3),
                        List.of(CardSubtype.VEHICLE),
                        Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                        null,
                        Set.of(),
                        GrantScope.OWN_PERMANENTS,
                        EffectDuration.UNTIL_END_OF_TURN,
                        noncreatureNonVehicleArtifact)));

        addEffect(EffectSlot.CHAOS_TRIGGERED, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.HAND)
                .filter(new CardAllOfPredicate(List.of(
                        new CardTypePredicate(CardType.ARTIFACT),
                        new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)))))
                .targetGraveyard(true)
                .build());
    }
}
