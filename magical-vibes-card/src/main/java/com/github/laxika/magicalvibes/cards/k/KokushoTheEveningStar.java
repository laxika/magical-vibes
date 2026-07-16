package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "DRB", collectorNumber = "9")
public class KokushoTheEveningStar extends Card {

    public KokushoTheEveningStar() {
        // When Kokusho dies, each opponent loses 5 life. You gain life equal to the life lost this way.
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(5, LoseLifeRecipient.EACH_OPPONENT, true));
    }
}
