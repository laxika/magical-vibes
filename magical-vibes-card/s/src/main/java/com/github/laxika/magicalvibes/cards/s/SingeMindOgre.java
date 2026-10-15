package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ARB", collectorNumber = "45")
public class SingeMindOgre extends Card {

    public SingeMindOgre() {
        // When this creature enters, target player reveals a card at random from their hand,
        // then loses life equal to that card's mana value.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new RevealRandomCardFromTargetPlayerHandLoseLifeEqualToManaValueEffect());
    }
}
