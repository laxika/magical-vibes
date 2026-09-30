package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawIfLibraryLargerElseMillOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "HBG", collectorNumber = "72")
public class JonIrenicusTheExile extends Card {

    public JonIrenicusTheExile() {
        var opponentTarget = target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ));

        opponentTarget.addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new DrawIfLibraryLargerElseMillOpponentsEffect());
    }
}
