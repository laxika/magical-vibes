package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.TargetPlayerIsActivePlayer;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetOpponentMayDrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIC", collectorNumber = "30")
@CardRegistration(set = "FIC", collectorNumber = "138")
@CardRegistration(set = "FIC", collectorNumber = "466")
public class TataruTaru extends Card {

    public TataruTaru() {
        // When Tataru Taru enters, you draw a card and target opponent may draw a card.
        var targetOpponentMayDrawCard = TargetOpponentMayDrawCardEffect.ofChosenTarget();
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).bindEffect(targetOpponentMayDrawCard);
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(), targetOpponentMayDrawCard);

        // Whenever an opponent draws a card, if it isn't that player's turn, create a tapped
        // Treasure token. This ability triggers only once each turn.
        addTrigger(Triggers.opponentDrawsCard(),
                TriggerOptions.DEFAULT.withInterveningIf(new NotCondition(new TargetPlayerIsActivePlayer()))
                        .withLimit(TriggerLimit.ONCE_EACH_TURN),
                CreateTokenEffect.ofTreasureToken(1, true));
    }
}
