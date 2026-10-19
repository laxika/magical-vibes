package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.condition.CreatureCardLeftGraveyardThisTurn;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCreatureDealsPowerDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsBattlePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "80")
@CardRegistration(set = "NCC", collectorNumber = "180")
public class SyrixCarrierOfTheFlame extends Card {

    public SyrixCarrierOfTheFlame() {
        setAllowSharedTargets(true);

        ControlledPermanentPredicateTargetFilter phoenixTarget = new ControlledPermanentPredicateTargetFilter(
                new PermanentHasSubtypePredicate(CardSubtype.PHOENIX),
                "Target must be a Phoenix you control");

        target(phoenixTarget);
        target(new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentIsBattlePredicate())),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be any target")).addEffect(EffectSlot.END_STEP_TRIGGERED,
                new ConditionalEffect(new CreatureCardLeftGraveyardThisTurn(),
                        new TargetCreatureDealsPowerDamageToAnyTargetEffect()));

        addEffect(EffectSlot.GRAVEYARD_ON_ALLY_CREATURE_DIES,
                new TriggeringCardConditionalEffect(
                        new CardSubtypePredicate(CardSubtype.PHOENIX),
                        new CastCardFromGraveyardEffect(new CardIsSelfPredicate(),
                                GraveyardSearchScope.CONTROLLERS_GRAVEYARD,
                                new CardNotPredicate(new CardTruePredicate()), false)));
    }
}
