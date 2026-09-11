package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndChangeLifeEffect;

@CardRegistration(set = "FIN", collectorNumber = "94")
@CardRegistration(set = "FIN", collectorNumber = "334")
@CardRegistration(set = "RAV", collectorNumber = "81")
@CardRegistration(set = "MM2", collectorNumber = "75")
@CardRegistration(set = "MMA", collectorNumber = "75")
public class DarkConfidant extends Card {

    public DarkConfidant() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new RevealTopCardPutIntoHandAndChangeLifeEffect(false));
    }
}
