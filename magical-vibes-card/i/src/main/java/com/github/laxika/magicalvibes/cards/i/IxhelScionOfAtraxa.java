package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect;

@CardRegistration(set = "ONC", collectorNumber = "1")
@CardRegistration(set = "ONC", collectorNumber = "29")
@CardRegistration(set = "ONC", collectorNumber = "37")
public class IxhelScionOfAtraxa extends Card {

    public IxhelScionOfAtraxa() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new ExileTopCardOfEachOpponentLibraryAndGrantPlayPermissionEffect(3));
    }
}
