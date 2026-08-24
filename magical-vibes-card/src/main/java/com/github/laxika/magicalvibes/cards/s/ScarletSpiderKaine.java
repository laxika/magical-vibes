package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardCardAndPutCounterOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "SPM", collectorNumber = "143")
public class ScarletSpiderKaine extends Card {

    public ScarletSpiderKaine() {
        // When Scarlet Spider enters, you may discard a card. If you do, put a +1/+1 counter on
        // him.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MayEffect(
                new DiscardCardAndPutCounterOnSourceEffect(null, "a card"),
                "Discard a card to put a +1/+1 counter on Scarlet Spider?"));
    }
}
