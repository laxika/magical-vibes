package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;

@CardRegistration(set = "M10", collectorNumber = "146")
@CardRegistration(set = "M11", collectorNumber = "149")
@CardRegistration(set = "4ED", collectorNumber = "208")
@CardRegistration(set = "ATH", collectorNumber = "43")
@CardRegistration(set = "BTD", collectorNumber = "41")
@CardRegistration(set = "SUM", collectorNumber = "162")
@CardRegistration(set = "ME1", collectorNumber = "102")
@CardRegistration(set = "3ED", collectorNumber = "162")
@CardRegistration(set = "PD2", collectorNumber = "17")
@CardRegistration(set = "MM2", collectorNumber = "122")
@CardRegistration(set = "A25", collectorNumber = "141")
@CardRegistration(set = "SLD", collectorNumber = "83")
@CardRegistration(set = "SLD", collectorNumber = "84")
@CardRegistration(set = "SLD", collectorNumber = "85")
@CardRegistration(set = "SLD", collectorNumber = "86")
@CardRegistration(set = "SLD", collectorNumber = "675")
@CardRegistration(set = "SLD", collectorNumber = "901")
@CardRegistration(set = "SLD", collectorNumber = "1638")
@CardRegistration(set = "SLD", collectorNumber = "1743")
@CardRegistration(set = "SLD", collectorNumber = "1822")
@CardRegistration(set = "SLD", collectorNumber = "1871")
@CardRegistration(set = "SLD", collectorNumber = "1879")
@CardRegistration(set = "SLD", collectorNumber = "2289")
@CardRegistration(set = "2X2", collectorNumber = "117")
@CardRegistration(set = "STA", collectorNumber = "42")
@CardRegistration(set = "GN3", collectorNumber = "83")
@CardRegistration(set = "FCA", collectorNumber = "40")
@CardRegistration(set = "TLE", collectorNumber = "32")
@CardRegistration(set = "SLZ", collectorNumber = "63")
@CardRegistration(set = "SLZ", collectorNumber = "184")
@CardRegistration(set = "SLZ", collectorNumber = "305")
@CardRegistration(set = "2ED", collectorNumber = "162")
public class LightningBolt extends Card {

    public LightningBolt() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
