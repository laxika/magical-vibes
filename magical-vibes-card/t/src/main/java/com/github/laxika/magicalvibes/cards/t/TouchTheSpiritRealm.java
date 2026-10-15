package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.FlickerEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "40")
public class TouchTheSpiritRealm extends Card {

    public TouchTheSpiritRealm() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()));
        PermanentPredicateTargetFilter artifactOrCreatureTarget = new PermanentPredicateTargetFilter(
                artifactOrCreature, "Target must be an artifact or creature");

        target(artifactOrCreatureTarget, 0, 1)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{1}{W}",
                List.of(FlickerEffect.exileTargetReturnAtEndStep()),
                "Channel — {1}{W}, Discard this card: Exile target artifact or creature. "
                        + "Return it to the battlefield under its owner's control at the beginning "
                        + "of the next end step.",
                artifactOrCreatureTarget));
    }
}
