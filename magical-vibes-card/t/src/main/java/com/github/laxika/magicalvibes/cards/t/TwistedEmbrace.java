package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "125")
public class TwistedEmbrace extends Card {

    public TwistedEmbrace() {
        PermanentPredicate artifactOrCreature = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsCreaturePredicate()
        ));
        target(new ControlledPermanentPredicateTargetFilter(
                artifactOrCreature,
                "Target must be an artifact or creature you control"
        )).addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                new PermanentIsCreaturePredicate(),
                new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                null
        ));

        PermanentPredicate creatureOrPlaneswalkerAnOpponentControls = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate()
                )),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
        ));
        TargetFilter etbTarget = new PermanentPredicateTargetFilter(
                creatureOrPlaneswalkerAnOpponentControls,
                "Target must be a creature or planeswalker an opponent controls"
        );
        target(etbTarget).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new DestroyTargetPermanentEffect(creatureOrPlaneswalkerAnOpponentControls));
    }
}
