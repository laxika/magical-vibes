package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CollectEvidenceCostPaid;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CollectEvidenceCost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "39")
public class BehindTheMask extends Card {

    public BehindTheMask() {
        addEffect(EffectSlot.SPELL, new CollectEvidenceCost(6, true));
        target(new PermanentPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new CollectEvidenceCostPaid(), animate(1, 1)))
                .addEffect(EffectSlot.SPELL, new ConditionalEffect(
                        new NotCondition(new CollectEvidenceCostPaid()), animate(4, 3)));
    }

    private static AnimatePermanentsEffect animate(int power, int toughness) {
        return new AnimatePermanentsEffect(
                power, toughness, List.of(), Set.of(), null,
                Set.of(CardType.ARTIFACT), GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN);
    }
}
