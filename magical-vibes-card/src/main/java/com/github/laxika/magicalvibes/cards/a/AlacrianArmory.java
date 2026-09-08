package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.TargetPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.BecomeSaddledUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "DFT", collectorNumber = "2")
public class AlacrianArmory extends Card {

    public AlacrianArmory() {
        addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(0, 1, Set.of(Keyword.VIGILANCE), GrantScope.OWN_CREATURES));

        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentHasSubtypePredicate(CardSubtype.MOUNT),
                                new PermanentHasSubtypePredicate(CardSubtype.VEHICLE))))),
                "Target must be a Mount or Vehicle you control"), 0, 1)
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        ConditionalEffect.unless(
                                new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.MOUNT)),
                                new BecomeSaddledUntilEndOfTurnEffect(GrantScope.TARGET)))
                .addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                        ConditionalEffect.unless(
                                new TargetPermanentMatches(new PermanentHasSubtypePredicate(CardSubtype.VEHICLE)),
                                new AnimatePermanentsEffect(null, null, List.of(), Set.of(), null, Set.of(),
                                        GrantScope.TARGET, EffectDuration.UNTIL_END_OF_TURN, null)));
    }
}
