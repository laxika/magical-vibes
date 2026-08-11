package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsAmongControlledPermanents;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "THS", collectorNumber = "89")
public class GrayMerchantOfAsphodel extends Card {

    public GrayMerchantOfAsphodel() {
        // Each opponent loses life equal to your black devotion, and you gain that much life.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LoseLifeEffect(
                new ColorManaSymbolsAmongControlledPermanents(ManaColor.BLACK),
                LoseLifeRecipient.EACH_OPPONENT,
                true));
    }
}
