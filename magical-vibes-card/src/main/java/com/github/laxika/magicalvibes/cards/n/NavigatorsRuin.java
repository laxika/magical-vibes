package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "XLN", collectorNumber = "63")
public class NavigatorsRuin extends Card {

    public NavigatorsRuin() {
        // Raid — At the beginning of your end step, if you attacked this turn,
        // target opponent mills four cards.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED, new RaidConditionalEffect(
                new MillTargetPlayerEffect(4)
        ));
        setCastTimeTargetFilter(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));
    }
}
