package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.NthCardDrawTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "HOB", collectorNumber = "15")
public class GleamingSplendor extends Card {

    public GleamingSplendor() {
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new NthCardDrawTriggerEffect(2, CreateTokenEffect.ofTreasureToken(1)));

        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player");
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{W}",
                List.of(
                        DrawCardForTargetPlayerEffect.forTargetGroup(1, 0),
                        DrawCardForTargetPlayerEffect.forTargetGroup(1, 1)
                ),
                "{2}{W}: Two target players each draw a card.",
                List.of(playerTarget, playerTarget),
                2,
                2
        ));
    }
}
