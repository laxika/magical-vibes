package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByActivePlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "FUT", collectorNumber = "70")
public class MagusOfTheAbyss extends Card {

    public MagusOfTheAbyss() {
        PermanentPredicate nonartifactCreatureControlledByActivePlayer = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentNotPredicate(new PermanentIsArtifactPredicate()),
                new PermanentControlledByActivePlayerPredicate()));

        target(new PermanentPredicateTargetFilter(
                nonartifactCreatureControlledByActivePlayer,
                "Target must be a nonartifact creature controlled by the active player"))
                .addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED,
                        DestroyTargetPermanentEffect.activePlayerChoosesTarget(
                                true, nonartifactCreatureControlledByActivePlayer));
    }
}
