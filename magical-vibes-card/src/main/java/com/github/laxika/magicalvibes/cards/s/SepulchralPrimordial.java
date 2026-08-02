package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "GTC", collectorNumber = "75")
public class SepulchralPrimordial extends Card {

    public SepulchralPrimordial() {
        // Intimidate is auto-loaded from Scryfall.
        // When this creature enters, for each opponent, you may put up to one target creature card
        // from that player's graveyard onto the battlefield under your control. With a single
        // opponent this is one up-to-one choice among that opponent's creature cards, made as the
        // trigger goes on the stack; declining leaves the trigger with no target.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new PutCardFromOpponentGraveyardOntoBattlefieldEffect(
                false, new CardTypePredicate(CardType.CREATURE), false));
    }
}
