package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsFromControllerGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "GTC", collectorNumber = "220")
public class ImmortalServitude extends Card {

    public ImmortalServitude() {
        // "Return each creature card with mana value X from your graveyard to the battlefield."
        // Not a choice and not targeted — every match comes back, so maxCount is unbounded.
        addEffect(EffectSlot.SPELL, new ReturnCardsFromControllerGraveyardToBattlefieldEffect(
                new CardTypePredicate(CardType.CREATURE), Integer.MAX_VALUE, true));
    }
}
