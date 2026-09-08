package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.LastDiscardedCardManaValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardThenEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;

@CardRegistration(set = "SOI", collectorNumber = "192")
public class WolfOfDevilsBreach extends Card {

    public WolfOfDevilsBreach() {
        // Whenever this creature attacks, you may pay {1}{R} and discard a card. If you do, this
        // creature deals damage to target creature or planeswalker equal to the discarded card's
        // mana value.
        addEffect(EffectSlot.ON_ATTACK, new MayPayManaEffect(
                "{1}{R}",
                new DiscardCardThenEffect(
                        null,
                        new DealDamageToTargetCreatureOrPlaneswalkerEffect(
                                new LastDiscardedCardManaValue()),
                        "a card"),
                "Pay {1}{R} and discard a card to deal damage to target creature or planeswalker?"));
    }
}
