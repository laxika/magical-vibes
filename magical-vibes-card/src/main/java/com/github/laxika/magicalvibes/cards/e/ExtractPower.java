package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffect;

@CardRegistration(set = "MSC", collectorNumber = "29")
@CardRegistration(set = "MSC", collectorNumber = "328")
public class ExtractPower extends Card {

    public ExtractPower() {
        addEffect(EffectSlot.SPELL,
                new ExileTopCardOfEachLibraryFaceDownAndGrantFreePlayPermissionEffect());
    }
}
