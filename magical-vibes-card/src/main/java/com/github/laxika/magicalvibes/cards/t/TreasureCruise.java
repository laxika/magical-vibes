package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KTK", collectorNumber = "59")
@CardRegistration(set = "UMA", collectorNumber = "79")
@CardRegistration(set = "TSR", collectorNumber = "319")
@CardRegistration(set = "PIO", collectorNumber = "79")
@CardRegistration(set = "MB2", collectorNumber = "37")
@CardRegistration(set = "NCC", collectorNumber = "237")
@CardRegistration(set = "SOC", collectorNumber = "205")
@CardRegistration(set = "C21", collectorNumber = "133")
@CardRegistration(set = "TDC", collectorNumber = "169")
@CardRegistration(set = "M3C", collectorNumber = "195")
@CardRegistration(set = "OTC", collectorNumber = "120")
public class TreasureCruise extends Card {

    public TreasureCruise() {
        addEffect(EffectSlot.SPELL, new DelveCost());
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
    }
}
