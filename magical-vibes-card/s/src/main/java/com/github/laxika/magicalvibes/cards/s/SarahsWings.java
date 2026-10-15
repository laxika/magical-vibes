package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantFlyingToTargetCreatureOrPlayerEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MB1", collectorNumber = "11")
public class SarahsWings extends Card {

    public SarahsWings() {
        // Target creature or player gains flying until end of turn.
        target(new AnyTargetPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a creature or player"
        )).addEffect(EffectSlot.SPELL, new GrantFlyingToTargetCreatureOrPlayerEffect());
    }
}
