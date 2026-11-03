package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "63")
public class NavigatorsRuin extends Card {

    public NavigatorsRuin() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // target opponent mills four cards.
        addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(new Raid()),
                new MillEffect(4, MillRecipient.TARGET_PLAYER));
        setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
    }
}
