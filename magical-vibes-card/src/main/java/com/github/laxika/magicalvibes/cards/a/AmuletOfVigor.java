package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UntapEnteringPermanentEffect;

@CardRegistration(set = "WWK", collectorNumber = "121")
public class AmuletOfVigor extends Card {

    public AmuletOfVigor() {
        addEffect(EffectSlot.ON_ANY_PERMANENT_ENTERS_BATTLEFIELD, new UntapEnteringPermanentEffect());
    }
}
