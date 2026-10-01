package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.HeistTargetOpponentLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "27")
public class TriumphantGetaway extends Card {

    public TriumphantGetaway() {
        // When Triumphant Getaway enters, heist target opponent's library twice.
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, SequenceEffect.of(
                        new HeistTargetOpponentLibraryEffect(),
                        new HeistTargetOpponentLibraryEffect()));

        // Whenever you cast a spell you don't own, each opponent loses 2 life and you gain 2 life.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL, new SpellCastTriggerEffect(
                new CardControllerDoesNotOwnPredicate(),
                List.of(
                        new LoseLifeEffect(2, LoseLifeRecipient.EACH_OPPONENT),
                        new GainLifeEffect(2))));
    }
}
