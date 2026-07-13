package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "251")
@CardRegistration(set = "7ED", collectorNumber = "246")
public class FyndhornElder extends Card {

    public FyndhornElder() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN, 2));
    }
}
