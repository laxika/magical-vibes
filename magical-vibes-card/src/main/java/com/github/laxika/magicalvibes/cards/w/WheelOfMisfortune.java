package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoblinGameEffect;
import com.github.laxika.magicalvibes.model.effect.WheelOfMisfortuneEffect;

@CardRegistration(set = "SLD", collectorNumber = "1300")
@CardRegistration(set = "SLD", collectorNumber = "1959")
@CardRegistration(set = "SLZ", collectorNumber = "70")
@CardRegistration(set = "SLZ", collectorNumber = "191")
@CardRegistration(set = "SLZ", collectorNumber = "312")
public class WheelOfMisfortune extends Card {

    public WheelOfMisfortune() {
        addEffect(EffectSlot.SPELL, GoblinGameEffect.wheelOfMisfortune());
    }
}
