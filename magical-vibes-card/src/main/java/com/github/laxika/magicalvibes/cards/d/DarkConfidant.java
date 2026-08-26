package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;

@CardRegistration(set = "FIN", collectorNumber = "94")
public class DarkConfidant extends Card {

    public DarkConfidant() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}
