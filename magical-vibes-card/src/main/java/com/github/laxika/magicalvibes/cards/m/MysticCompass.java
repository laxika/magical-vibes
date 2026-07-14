package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "302")
public class MysticCompass extends Card {

    public MysticCompass() {
        // {1}, {T}: Target land becomes the basic land type of your choice until end of turn.
        // Type-replacing (rule 305.7): the land loses its other land types/mana ability.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new GrantBasicLandTypeToTargetEffect(EffectDuration.UNTIL_END_OF_TURN, null, true)),
                "{1}, {T}: Target land becomes the basic land type of your choice until end of turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsLandPredicate(),
                        "Target must be a land"
                )
        ));
    }
}
