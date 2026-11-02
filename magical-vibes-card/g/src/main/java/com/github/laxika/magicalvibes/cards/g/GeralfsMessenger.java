package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DKA", collectorNumber = "63")
@CardRegistration(set = "SLD", collectorNumber = "842")
public class GeralfsMessenger extends Card {

    public GeralfsMessenger() {
        // Geralf's Messenger enters the battlefield tapped.
        addStaticEffect(new EntersTappedEffect());

        // When Geralf's Messenger enters the battlefield, target opponent loses 2 life.
        // Undying is loaded from Scryfall and resolved automatically when it dies.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
