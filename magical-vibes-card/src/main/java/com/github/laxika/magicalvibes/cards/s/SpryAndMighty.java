package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseTwoCreaturesByPowerDifferenceEffect;

@CardRegistration(set = "ECL", collectorNumber = "195")
public class SpryAndMighty extends Card {

    public SpryAndMighty() {
        addEffect(EffectSlot.SPELL, new ChooseTwoCreaturesByPowerDifferenceEffect());
    }
}
