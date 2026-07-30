package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "AVR", collectorNumber = "78")
public class StolenGoods extends Card {

    public StolenGoods() {
        // Target opponent exiles cards from the top of their library until they exile a nonland
        // card. Until end of turn, you may cast that card without paying its mana cost.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Must target an opponent"
        )).addEffect(EffectSlot.SPELL, new ExileTopUntilNonlandOfTargetOpponentMayCastThisTurnEffect());
    }
}
