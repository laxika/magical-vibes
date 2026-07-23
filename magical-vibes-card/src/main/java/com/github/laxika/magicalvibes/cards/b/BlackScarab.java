package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.OpponentControlsPermanent;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesMatchingPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "6")
public class BlackScarab extends Card {

    public BlackScarab() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // Enchanted creature can't be blocked by black creatures.
                .addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesMatchingPredicateEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK))))
                // Enchanted creature gets +2/+2 as long as an opponent controls a black permanent.
                .addEffect(EffectSlot.STATIC, new ConditionalEffect(
                        new OpponentControlsPermanent(new PermanentColorInPredicate(Set.of(CardColor.BLACK))),
                        new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE)));
    }
}
