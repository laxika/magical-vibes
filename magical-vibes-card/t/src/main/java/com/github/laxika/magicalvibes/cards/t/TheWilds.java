package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOC", collectorNumber = "71")
public class TheWilds extends Card {

    public TheWilds() {
        CreateTokenEffect food = CreateTokenEffect.ofFoodToken(1);
        addTrigger(Triggers.planeswalkToThis(), food);
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), food);
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"))
                .addTrigger(Triggers.chaosEnsues(),
                        new TargetPlayerSacrificesCreatureThenCreateTokensIfToughnessAtLeastEffect(
                                food, 4, 1, 2));
    }
}
