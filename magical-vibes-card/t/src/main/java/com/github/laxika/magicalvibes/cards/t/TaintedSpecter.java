package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromHandOnTopOfLibraryCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "148")
public class TaintedSpecter extends Card {

    public TaintedSpecter() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}{B}",
                List.of(
                        UnlessPaysEffect.unlessPays(Subjects.targetPlayer(), new PutCardFromHandOnTopOfLibraryCost(),
                                new DiscardEffect(1, Subjects.targetPlayer())),
                        new ConditionalEffect(HappenedThisWay.discarded(),
                                new MassDamageEffect(new Fixed(1), true))
                ),
                "{1}{B}{B}, {T}: Target player discards a card unless they put a card from their hand on top of their library. "
                        + "If that player discards a card this way, this creature deals 1 damage to each creature and each player. "
                        + "Activate only as a sorcery.",
                new PlayerPredicateTargetFilter(
                        new PlayerRelationPredicate(PlayerRelation.ANY),
                        "Target must be a player"
                ),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }
}
