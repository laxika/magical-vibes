package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForbiddenRitualEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "VIS", collectorNumber = "60")
public class ForbiddenRitual extends Card {

    public ForbiddenRitual() {
        // Sacrifice a nontoken permanent. If you do, target opponent loses 2 life unless that
        // player sacrifices a permanent of their choice or discards a card. You may repeat this
        // process any number of times.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        )).addEffect(EffectSlot.SPELL, new ForbiddenRitualEffect(2));
    }
}
