package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEC", collectorNumber = "15")
@CardRegistration(set = "NEC", collectorNumber = "51")
public class KatsumasaTheAnimator extends Card {

    public KatsumasaTheAnimator() {
        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        ));

        target(new PermanentPredicateTargetFilter(noncreatureArtifact,
                "Target must be a noncreature artifact"), 0, 3)
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new AnimatePermanentsEffect(
                                null, null, List.of(), Set.of(Keyword.FLYING), null, Set.of(),
                                GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, noncreatureArtifact),
                        new ConditionalEffect(
                                new NotCondition(new TargetPermanentMatches(
                                        new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))),
                                new SetBasePowerToughnessEffect(1, 1,
                                        EffectDuration.UNTIL_END_OF_TURN, noncreatureArtifact))
                ),
                "{2}{U}: Until end of turn, target noncreature artifact you control becomes an artifact creature and gains flying. If it's not a Vehicle, it has base power and toughness 1/1 until end of turn.",
                new ControlledPermanentPredicateTargetFilter(noncreatureArtifact,
                        "Target must be a noncreature artifact you control")
        ));
    }
}
