package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "PIP", collectorNumber = "160")
@CardRegistration(set = "PIP", collectorNumber = "449")
@CardRegistration(set = "PIP", collectorNumber = "688")
@CardRegistration(set = "PIP", collectorNumber = "977")
public class EntrapmentManeuver extends Card {

    public EntrapmentManeuver() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addEffect(EffectSlot.SPELL,
                new TargetPlayerSacrificesAttackingCreatureThenCreateTokensEqualToToughnessEffect(
                        CreateTokenEffect.whiteSoldier(1)));
    }
}
