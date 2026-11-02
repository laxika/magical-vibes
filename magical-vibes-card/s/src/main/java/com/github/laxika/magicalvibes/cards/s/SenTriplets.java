package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SenTripletsUpkeepEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "109")
@CardRegistration(set = "SLD", collectorNumber = "1198")
@CardRegistration(set = "2XM", collectorNumber = "218")
public class SenTriplets extends Card {

    public SenTriplets() {
        // At the beginning of your upkeep, choose target opponent. This turn, that player can't cast
        // spells or activate abilities and plays with their hand revealed. You may play lands and cast
        // spells from that player's hand this turn.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.upkeep(ActivePlayer.YOU), new SenTripletsUpkeepEffect());
    }
}
