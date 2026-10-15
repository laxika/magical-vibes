package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPermanentIntoLibraryNFromTopOrBottomEffect;
import com.github.laxika.magicalvibes.model.effect.SpreeAdditionalManaCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "53")
public class JailbreakScheme extends Card {

    public JailbreakScheme() {
        addEffect(EffectSlot.SPELL, new SpreeAdditionalManaCost(List.of("{3}", "{2}")));
        setAllowSharedTargets(true);

        var artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));

        addEffect(EffectSlot.SPELL, ChooseOneEffect.oneOrMore(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Put a +1/+1 counter on target creature. It can't be blocked this turn",
                        List.of(
                                new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE),
                                new MakeCreatureUnblockableEffect()),
                        new PermanentPredicateTargetFilter(
                                new PermanentIsCreaturePredicate(), "Target must be a creature.")),
                new ChooseOneEffect.ChooseOneOption(
                        "Target artifact or creature's owner puts it on their choice of the top or bottom of their library",
                        new PutTargetPermanentIntoLibraryNFromTopOrBottomEffect(0, artifactOrCreature),
                        new PermanentPredicateTargetFilter(artifactOrCreature,
                                "Target must be an artifact or creature."))
        )));
    }
}
