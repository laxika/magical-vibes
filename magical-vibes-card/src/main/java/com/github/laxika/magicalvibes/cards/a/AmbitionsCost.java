package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "67")
@CardRegistration(set = "8ED", collectorNumber = "118")
@CardRegistration(set = "DDR", collectorNumber = "38")
@CardRegistration(set = "C15", collectorNumber = "113")
@CardRegistration(set = "HBG", collectorNumber = "140")
@CardRegistration(set = "MOC", collectorNumber = "246")
@CardRegistration(set = "C21", collectorNumber = "134")
@CardRegistration(set = "DMC", collectorNumber = "110")
@CardRegistration(set = "C20", collectorNumber = "129")
@CardRegistration(set = "KHC", collectorNumber = "47")
@CardRegistration(set = "C17", collectorNumber = "95")
@CardRegistration(set = "SCD", collectorNumber = "66")
public class AmbitionsCost extends Card {

    public AmbitionsCost() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(3));
    }
}
