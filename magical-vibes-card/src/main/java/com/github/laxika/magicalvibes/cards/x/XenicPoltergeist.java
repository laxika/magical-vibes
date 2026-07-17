package com.github.laxika.magicalvibes.cards.x;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "206")
public class XenicPoltergeist extends Card {

    public XenicPoltergeist() {
        // {T}: Until your next upkeep, target noncreature artifact becomes an artifact creature with
        // power and toughness each equal to its mana value.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AnimatePermanentsEffect(
                        new TargetManaValue(), new TargetManaValue(),
                        List.of(), Set.of(), null, Set.of(),
                        GrantScope.TARGET, EffectDuration.UNTIL_YOUR_NEXT_TURN, null
                )),
                "{T}: Until your next upkeep, target noncreature artifact becomes an artifact creature with power and toughness each equal to its mana value.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentNotPredicate(new PermanentIsCreaturePredicate())
                        )),
                        "Target must be a noncreature artifact"
                )
        ));
    }
}
