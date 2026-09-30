package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "YLCI", collectorNumber = "30")
public class TanJolomTheWorldwalker extends Card {

    public TanJolomTheWorldwalker() {
        target(new PermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentControlledBySourceControllerPredicate(),
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsLandPredicate()
                        )),
                        new PermanentNotPredicate(new PermanentIsCreaturePredicate()),
                        new PermanentNotPredicate(new PermanentIsTokenPredicate())
                )),
                "Target must be a noncreature, nontoken artifact or land you control"
        ), 0, 1).addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new AnimatePermanentsEffect(
                        3, 3,
                        List.of(CardSubtype.SPIRIT),
                        Set.of(Keyword.VIGILANCE, Keyword.DOUBLE_TEAM),
                        null,
                        Set.of(CardType.CREATURE),
                        GrantScope.TARGET,
                        EffectDuration.UNTIL_END_OF_TURN
                ));
    }
}
