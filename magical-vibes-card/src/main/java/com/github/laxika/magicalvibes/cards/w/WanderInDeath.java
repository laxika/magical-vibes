package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;


@CardRegistration(set = "AKH", collectorNumber = "115")
public class WanderInDeath extends Card {

    public WanderInDeath() {
        // Return up to two target creature cards from your graveyard to your hand.
        addEffect(EffectSlot.SPELL, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.CREATURE), 2));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
