package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "HOB", collectorNumber = "76")
public class InsideInformation extends Card {

    public InsideInformation() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT), "Target must be an opponent"));
        addEffect(EffectSlot.SPELL, new ExileTopCardsOfTargetPlayerMayPlayForLifeThisTurnEffect(new XValue()));
    }
}
