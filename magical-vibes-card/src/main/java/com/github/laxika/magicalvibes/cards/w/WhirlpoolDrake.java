package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleHandIntoLibraryAndDrawEffect;

@CardRegistration(set = "APC", collectorNumber = "34")
public class WhirlpoolDrake extends Card {

    public WhirlpoolDrake() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ShuffleHandIntoLibraryAndDrawEffect(false));
        addEffect(EffectSlot.ON_DEATH, new ShuffleHandIntoLibraryAndDrawEffect(false));
    }
}
