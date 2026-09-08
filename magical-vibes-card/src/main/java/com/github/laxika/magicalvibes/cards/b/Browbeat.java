package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "TSB", collectorNumber = "56")
@CardRegistration(set = "JUD", collectorNumber = "82")
public class Browbeat extends Card {

    public Browbeat() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"));
        addEffect(EffectSlot.SPELL, new AnyPlayerMayTakeDamageOrTargetPlayerDrawEffect(5, 3));
    }
}
