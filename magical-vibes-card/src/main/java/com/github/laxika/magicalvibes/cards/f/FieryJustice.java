package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "ICE", collectorNumber = "288")
@CardRegistration(set = "TSB", collectorNumber = "92")
public class FieryJustice extends Card {

    public FieryJustice() {
        // Fiery Justice deals 5 damage divided as you choose among any number of targets.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.chosenAmongAnyTargets(5));
        // Target opponent gains 5 life.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.SPELL, new TargetPlayerGainsLifeEffect(5));
    }
}
