package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReturnedPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "58")
@CardRegistration(set = "MOC", collectorNumber = "233")
public class RiseAndShine extends Card {

    public RiseAndShine() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{4}{U}{U}"))));

        PermanentPredicate noncreatureArtifact = new PermanentAllOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
        ));

        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                SequenceEffect.of(
                        new AnimatePermanentsEffect(
                                0, 0, List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                                GrantScope.TARGET, EffectDuration.PERMANENT),
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE, 4)),
                SequenceEffect.of(
                        new AnimatePermanentsEffect(
                                new Fixed(0), new Fixed(0), List.of(), Set.of(), null, Set.of(CardType.CREATURE),
                                GrantScope.OWN_PERMANENTS, EffectDuration.PERMANENT,
                                noncreatureArtifact),
                        new PutCounterOnReturnedPermanentsEffect(CounterType.PLUS_ONE_PLUS_ONE, 4))));

        target(new ControlledPermanentPredicateTargetFilter(
                noncreatureArtifact,
                "Target must be a noncreature artifact you control"));
    }
}
