package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TotalPermanentCountEven;
import com.github.laxika.magicalvibes.model.effect.CanAttackAsThoughHasteUnlessEnteredThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ICE", collectorNumber = "178")
public class ChaosLord extends Card {

    public ChaosLord() {
        // The "if the number of permanents is even" clause sits at the end of the ability, so it is
        // not an intervening-if: the target opponent is chosen every upkeep and the parity is only
        // checked on resolution.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new TotalPermanentCountEven(), new TargetPlayerGainsControlOfSourceCreatureEffect()));
        addStaticEffect(new CanAttackAsThoughHasteUnlessEnteredThisTurnEffect());
    }
}
