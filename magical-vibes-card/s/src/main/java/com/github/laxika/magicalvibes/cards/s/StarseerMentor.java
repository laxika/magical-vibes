package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AnyOf;
import com.github.laxika.magicalvibes.model.condition.ControllerDidntLoseLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.GainedLifeThisTurn;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessSacrificeNonlandOrDiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BLB", collectorNumber = "233")
public class StarseerMentor extends Card {

    public StarseerMentor() {
        // At the beginning of your end step, if you gained or lost life this turn, target opponent
        // loses 3 life unless they sacrifice a nonland permanent of their choice or discard a card.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                TriggerOptions.DEFAULT.withInterveningIf(
                        new AnyOf(List.of(
                                new GainedLifeThisTurn(),
                                new NotCondition(new ControllerDidntLoseLifeThisTurn())))),
                new LoseLifeUnlessSacrificeNonlandOrDiscardEffect(3, true));
    }
}
