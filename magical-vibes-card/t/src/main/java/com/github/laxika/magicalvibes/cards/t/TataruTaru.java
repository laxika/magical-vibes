package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActivePlayer;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "FIC", collectorNumber = "30")
@CardRegistration(set = "FIC", collectorNumber = "138")
@CardRegistration(set = "FIC", collectorNumber = "466")
public class TataruTaru extends Card {

    public TataruTaru() {
        // When Tataru Taru enters, you draw a card and target opponent may draw a card.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, TargetOpponentMayDrawCardEffect.ofChosenTarget());

        // Whenever an opponent draws a card, if it isn't that player's turn, create a tapped
        // Treasure token. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_OPPONENT_DRAWS,
                new OncePerTurnTriggerEffect(new ConditionalEffect(
                        new NotCondition(new TargetPlayerIsActivePlayer()),
                        CreateTokenEffect.ofTreasureToken(1, true))));
    }
}
