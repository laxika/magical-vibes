package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.Overloaded;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachOtherControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "BRC", collectorNumber = "8")
@CardRegistration(set = "BRC", collectorNumber = "55")
public class MarchOfProgress extends Card {

    public MarchOfProgress() {
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{6}{U}"))));
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(
                new Overloaded(),
                new CreateTokenCopyOfTargetPermanentEffect(),
                new CreateTokenCopyOfEachOtherControlledPermanentEffect(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate())))));
        target(new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate())),
                "Target must be an artifact creature you control"));
    }
}
