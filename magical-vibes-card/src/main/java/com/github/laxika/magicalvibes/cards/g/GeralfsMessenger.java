package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

@CardRegistration(set = "DKA", collectorNumber = "63")
public class GeralfsMessenger extends Card {

    public GeralfsMessenger() {
        // Geralf's Messenger enters the battlefield tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // When Geralf's Messenger enters the battlefield, target opponent loses 2 life.
        // Undying is loaded from Scryfall and resolved automatically when it dies.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TargetPlayerLosesLifeEffect(2));
    }
}
