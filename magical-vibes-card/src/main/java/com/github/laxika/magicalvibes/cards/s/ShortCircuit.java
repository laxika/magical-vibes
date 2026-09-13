package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "78")
public class ShortCircuit extends Card {

    public ShortCircuit() {
        var creaturePredicate = new PermanentIsCreaturePredicate();
        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                creaturePredicate));

        target(new PermanentPredicateTargetFilter(artifactOrCreature,
                "Target must be an artifact or creature"))
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(-3, 0, GrantScope.ENCHANTED_CREATURE, creaturePredicate))
                .addEffect(EffectSlot.STATIC,
                        new RemoveKeywordEffect(Keyword.FLYING, GrantScope.ENCHANTED_CREATURE,
                                creaturePredicate, EffectDuration.PERMANENT));
    }
}
