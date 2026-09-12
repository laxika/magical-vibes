package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongControlledCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasAnySubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "21")
public class InvokeJustice extends Card {

    public InvokeJustice() {
        target(new GraveyardCardPredicateTargetFilter(
                new CardIsPermanentPredicate(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addEffect(EffectSlot.SPELL, ReturnCardFromGraveyardEffect.builder()
                .destination(GraveyardChoiceDestination.BATTLEFIELD)
                .filter(new CardIsPermanentPredicate())
                .targetGraveyard(true)
                .build());

        PermanentPredicate creatureOrVehicle = new PermanentAnyOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentHasAnySubtypePredicate(Set.of(CardSubtype.VEHICLE))
        ));
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY), "Target must be a player"))
                .addEffect(EffectSlot.SPELL, new DistributeCountersAmongControlledCreaturesEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, 4, creatureOrVehicle).forTargetPlayer());
    }
}
