package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.PermanentLeftBattlefieldUnderYourControlThisTurn;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "TMT", collectorNumber = "163")
public class PizzaFaceGastromancer extends Card {

    public PizzaFaceGastromancer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, foodToken());

        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PermanentPredicate otherArtifactOrCreature = new PermanentAllOfPredicate(List.of(
                artifactOrCreature,
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())));

        target(new PermanentPredicateTargetFilter(
                otherArtifactOrCreature,
                "Target must be another artifact or creature"), 0, 1)
                .addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                        new ConditionalEffect(
                                new PermanentLeftBattlefieldUnderYourControlThisTurn(),
                                SequenceEffect.of(
                                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 3),
                                        new ConditionalEffect(
                                                new NotCondition(new TargetPermanentMatches(
                                                        new PermanentIsCreaturePredicate())),
                                                new AnimatePermanentsEffect(
                                                        0,
                                                        0,
                                                        List.of(CardSubtype.MUTANT),
                                                        Set.of(),
                                                        null,
                                                        Set.of(CardType.CREATURE),
                                                        GrantScope.TARGET,
                                                        EffectDuration.PERMANENT)))));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{10}",
                List.of(new SacrificeSelfCost(), new GainLifeEffect(15)),
                "{10}, {T}, Sacrifice Pizza Face: You gain 15 life."
        ));
    }

    private static CreateTokenEffect foodToken() {
        return CreateTokenEffect.ofArtifactToken(1, "Food", List.of(CardSubtype.FOOD), List.of(
                new ActivatedAbility(
                        true,
                        "{2}",
                        List.of(new SacrificeSelfCost(), new GainLifeEffect(3)),
                        "{2}, {T}, Sacrifice this token: You gain 3 life."
                )));
    }
}
