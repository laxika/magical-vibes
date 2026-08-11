package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;

@CardRegistration(set = "THS", collectorNumber = "121")
public class FanaticOfMogis extends Card {

    public FanaticOfMogis() {
        // When this creature enters, it deals damage to each opponent equal to your red devotion.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DealDamageToPlayersEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.RED),
                DamageRecipient.EACH_OPPONENT));
    }
}
