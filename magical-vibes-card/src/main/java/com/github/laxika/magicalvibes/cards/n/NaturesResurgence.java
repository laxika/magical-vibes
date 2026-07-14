package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "7ED", collectorNumber = "259")
@CardRegistration(set = "6ED", collectorNumber = "242")
public class NaturesResurgence extends Card {

    public NaturesResurgence() {
        // Each player draws a card for each creature card in their graveyard.
        // EachPlayerDrawsCardEffect re-evaluates the amount per drawing player, so
        // CONTROLLER scope counts each player's own graveyard.
        addEffect(EffectSlot.SPELL, new EachPlayerDrawsCardEffect(
                new CardsInGraveyard(new CardTypePredicate(CardType.CREATURE), CountScope.CONTROLLER)));
    }
}
