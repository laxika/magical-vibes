package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessUntilEndOfTurnEffect;

@CardRegistration(set = "M11", collectorNumber = "52")
public class Diminish extends Card {

    public Diminish() {
        addEffect(EffectSlot.SPELL, new SetBasePowerToughnessUntilEndOfTurnEffect(1, 1));
    }
}
