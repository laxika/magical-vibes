package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerAndDrawPerTypeMilledEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "M19", collectorNumber = "67")
public class PatientRebuilding extends Card {

    public PatientRebuilding() {
        // At the beginning of your upkeep, target opponent mills three cards, then you draw a card
        // for each land card put into their graveyard this way.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.UPKEEP_TRIGGERED,
                        new MillTargetPlayerAndDrawPerTypeMilledEffect(3, CardType.LAND));
    }
}
