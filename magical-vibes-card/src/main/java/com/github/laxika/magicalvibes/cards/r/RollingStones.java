package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MatchingCreaturesCanAttackAsThoughNoDefenderEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "8ED", collectorNumber = "38")
public class RollingStones extends Card {

    public RollingStones() {
        // Wall creatures can attack as though they didn't have defender.
        addEffect(EffectSlot.STATIC, new MatchingCreaturesCanAttackAsThoughNoDefenderEffect(
                new PermanentHasSubtypePredicate(CardSubtype.WALL)));
    }
}
