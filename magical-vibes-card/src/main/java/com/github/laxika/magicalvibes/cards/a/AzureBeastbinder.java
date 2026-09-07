package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "41")
public class AzureBeastbinder extends Card {

    public AzureBeastbinder() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                new PermanentPowerAtLeastPredicate(2)));

        PermanentPredicate targetPermanent = new PermanentAllOfPredicate(List.of(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsArtifactPredicate(),
                        new PermanentIsCreaturePredicate(),
                        new PermanentIsPlaneswalkerPredicate())),
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
        target(new PermanentPredicateTargetFilter(
                targetPermanent,
                "Target must be an artifact, creature, or planeswalker an opponent controls"), 0, 1)
                .addEffect(EffectSlot.ON_ATTACK,
                        new LosesAllAbilitiesEffect(GrantScope.TARGET, EffectDuration.UNTIL_YOUR_NEXT_TURN))
                .addEffect(EffectSlot.ON_ATTACK,
                        new SetBasePowerToughnessEffect(2, 2, EffectDuration.UNTIL_YOUR_NEXT_TURN,
                                new PermanentIsCreaturePredicate()));
    }
}
