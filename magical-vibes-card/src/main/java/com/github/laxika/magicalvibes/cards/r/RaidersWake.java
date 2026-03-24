package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "XLN", collectorNumber = "116")
public class RaidersWake extends Card {

    public RaidersWake() {
        // Whenever an opponent discards a card, that player loses 2 life.
        addEffect(EffectSlot.ON_OPPONENT_DISCARDS, new LoseLifeEffect(2));
        // Raid — At the beginning of your end step, if you attacked this turn,
        // target opponent discards a card.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new RaidConditionalEffect(
                new TargetPlayerDiscardsEffect(1)
        ));
        setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
    }
}
