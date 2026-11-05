package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.ControlsCreatureWithGreatestPower;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AVR", collectorNumber = "122")
public class TriumphOfCruelty extends Card {

    public TriumphOfCruelty() {
        // At the beginning of your upkeep, target opponent discards a card if you control the
        // creature with the greatest power or tied for the greatest power. The "if" clause is not
        // an intervening-if (it does not follow the trigger event), so the ability always triggers
        // and targets; the condition is checked on resolution.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ConditionalEffect(
                new ControlsCreatureWithGreatestPower(),
                new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
