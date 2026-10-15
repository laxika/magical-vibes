package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "M11", collectorNumber = "52")
@CardRegistration(set = "IMA", collectorNumber = "50")
public class Diminish extends Card {

    public Diminish() {
        addEffect(EffectSlot.SPELL, new SetBasePowerToughnessEffect(1, 1));
    }
}
