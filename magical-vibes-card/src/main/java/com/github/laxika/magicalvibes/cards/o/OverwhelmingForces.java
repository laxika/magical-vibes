package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PTK", collectorNumber = "79")
public class OverwhelmingForces extends Card {

    public OverwhelmingForces() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "You must target an opponent."
        ))
                .addEffect(EffectSlot.SPELL, new DestroyCreaturesTargetPlayerControlsAndDrawPerDestroyedEffect(1));
    }
}
