package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasGreatestPowerAmongAllCreaturesPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "192")
public class HistoriansWisdom extends Card {

    public HistoriansWisdom() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));

        target(new PermanentPredicateTargetFilter(
                artifactOrCreature,
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new TargetPermanentMatches(new PermanentHasGreatestPowerAmongAllCreaturesPredicate()),
                        new DrawCardEffect()))
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
