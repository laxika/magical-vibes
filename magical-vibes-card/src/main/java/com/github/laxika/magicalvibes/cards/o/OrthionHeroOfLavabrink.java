package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "MOM", collectorNumber = "334")
@CardRegistration(set = "MOM", collectorNumber = "379")
public class OrthionHeroOfLavabrink extends Card {

    public OrthionHeroOfLavabrink() {
        PermanentPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
        ));
        PermanentPredicateTargetFilter targetFilter = new PermanentPredicateTargetFilter(
                anotherCreatureYouControl, "Target must be another creature you control");

        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{R}",
                List.of(tokenCopyEffect(1)),
                "{1}{R}, {T}: Create a token that's a copy of another target creature you control. "
                        + "It gains haste. Sacrifice it at the beginning of the next end step. Activate only as a sorcery.",
                targetFilter,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{6}{R}{R}{R}",
                List.of(tokenCopyEffect(5)),
                "{6}{R}{R}{R}, {T}: Create five tokens that are copies of another target creature you control. "
                        + "They gain haste. Sacrifice them at the beginning of the next end step. Activate only as a sorcery.",
                targetFilter,
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static CreateTokenCopyOfTargetPermanentEffect tokenCopyEffect(int amount) {
        return new CreateTokenCopyOfTargetPermanentEffect(
                List.of(), Set.of(), null, null, Map.of(), true, false, true, false,
                false, false, null, Set.of(), false, Map.of(), List.of(), false, false,
                new Fixed(amount), false, Set.of(), false);
    }
}
