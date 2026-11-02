package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerDiscardsHandThenDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerHasMoreCardsInHandThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "42")
public class OathOfScholars extends Card {

    public OathOfScholars() {
        target(new PlayerPredicateTargetFilter(
                new PlayerHasMoreCardsInHandThanControllerPredicate(1, true),
                "Target player must be an opponent with more cards in hand than the current player"
        )).addTrigger(Triggers.upkeep(ActivePlayer.ANY), new MayEffect(
                new ActivePlayerDiscardsHandThenDrawsEffect(3),
                "Discard your hand and draw three cards?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
