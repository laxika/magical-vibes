package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M11", collectorNumber = "70")
@CardRegistration(set = "DDI", collectorNumber = "24")
@CardRegistration(set = "SLD", collectorNumber = "186")
@CardRegistration(set = "SLD", collectorNumber = "1719")
@CardRegistration(set = "SLD", collectorNumber = "2301")
@CardRegistration(set = "SOA", collectorNumber = "21")
@CardRegistration(set = "C15", collectorNumber = "101")
@CardRegistration(set = "WHO", collectorNumber = "218")
@CardRegistration(set = "WHO", collectorNumber = "809")
@CardRegistration(set = "MB2", collectorNumber = "35")
@CardRegistration(set = "NCC", collectorNumber = "230")
@CardRegistration(set = "LTC", collectorNumber = "196")
@CardRegistration(set = "TDC", collectorNumber = "161")
@CardRegistration(set = "OTC", collectorNumber = "107")
@CardRegistration(set = "BRC", collectorNumber = "92")
public class Preordain extends Card {

    public Preordain() {
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
