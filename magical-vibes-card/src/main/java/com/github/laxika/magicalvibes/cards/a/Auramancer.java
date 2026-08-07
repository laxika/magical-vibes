package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "M12", collectorNumber = "9")
@CardRegistration(set = "M14", collectorNumber = "6")
@CardRegistration(set = "ORI", collectorNumber = "5")
public class Auramancer extends Card {

    public Auramancer() {
        // When this creature enters, you may return target enchantment card
        // from your graveyard to your hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ReturnTargetCardsFromGraveyardToHandEffect(
                new CardTypePredicate(CardType.ENCHANTMENT), 1));
    }
}
