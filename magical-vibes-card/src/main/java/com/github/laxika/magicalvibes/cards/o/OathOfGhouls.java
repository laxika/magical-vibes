package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MayChoicePlayer;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerReturnsCardFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;

@CardRegistration(set = "EXO", collectorNumber = "69")
public class OathOfGhouls extends Card {

    public OathOfGhouls() {
        target(new PlayerPredicateTargetFilter(
                new PlayerHasFewerCreatureCardsInGraveyardThanControllerPredicate(1, true),
                "Target player must be an opponent with fewer creature cards in their graveyard than the current player"
        )).addEffect(EffectSlot.EACH_UPKEEP_TRIGGERED, new MayEffect(
                new ActivePlayerReturnsCardFromGraveyardToHandEffect(new CardTypePredicate(CardType.CREATURE)),
                "Return a creature card from your graveyard to your hand?",
                null,
                MayChoicePlayer.ACTIVE_PLAYER));
    }
}
