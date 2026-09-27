package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "MOC", collectorNumber = "71")
public class TheWilds extends Card {

    public TheWilds() {
        CreateTokenEffect food = CreateTokenEffect.ofFoodToken(1);
        addEffect(EffectSlot.PLANESWALK_TO_TRIGGERED, food);
        addEffect(EffectSlot.UPKEEP_TRIGGERED, food);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addEffect(EffectSlot.CHAOS_TRIGGERED,
                        new TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect(
                                food, 4, 1, 2));
    }
}
