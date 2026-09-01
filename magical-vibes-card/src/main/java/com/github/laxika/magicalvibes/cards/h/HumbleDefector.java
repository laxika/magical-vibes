package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "FRF", collectorNumber = "104")
public class HumbleDefector extends Card {

    public HumbleDefector() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DrawCardEffect(2), new TargetPlayerGainsControlOfSourceCreatureEffect()),
                "{T}: Draw two cards. Target opponent gains control of this creature. Activate only during your turn.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                        "Target must be an opponent"
                ),
                null, null,
                ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ));
    }
}
