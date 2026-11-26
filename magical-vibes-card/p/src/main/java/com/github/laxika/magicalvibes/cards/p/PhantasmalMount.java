package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedEventTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "88")
@CardRegistration(set = "ME2", collectorNumber = "59")
public class PhantasmalMount extends Card {

    public PhantasmalMount() {
        // Flying is auto-loaded from Scryfall.
        // {T}: Target creature you control with toughness 2 or less gets +1/+1 and gains flying
        // until end of turn. When this creature leaves the battlefield this turn, sacrifice that
        // creature. When the creature leaves the battlefield this turn, sacrifice this creature.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        BoostEffect.target(1, 1),
                        new GrantKeywordEffect(Keyword.FLYING, Subjects.target()),
                        RegisterDelayedEventTriggerEffect.aboutWhenSourceLeavesThisTurn(Subjects.target(),
                                SacrificeSelfEffect.ofTriggeringPermanent()),
                        RegisterDelayedEventTriggerEffect.whenLeavesThisTurn(Subjects.target(),
                                new SacrificeSelfEffect())
                ),
                "{T}: Target creature you control with toughness 2 or less gets +1/+1 and gains flying "
                        + "until end of turn. When this creature leaves the battlefield this turn, "
                        + "sacrifice that creature. When the creature leaves the battlefield this turn, "
                        + "sacrifice this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledBySourceControllerPredicate(),
                                new PermanentToughnessAtMostPredicate(2))),
                        "Target must be a creature you control with toughness 2 or less")));
    }
}
