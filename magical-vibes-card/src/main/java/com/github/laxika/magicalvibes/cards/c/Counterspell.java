package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "4ED", collectorNumber = "65")
@CardRegistration(set = "2ED", collectorNumber = "55")
@CardRegistration(set = "3ED", collectorNumber = "54")
@CardRegistration(set = "5ED", collectorNumber = "77")
@CardRegistration(set = "6ED", collectorNumber = "61")
@CardRegistration(set = "7ED", collectorNumber = "67")
@CardRegistration(set = "ICE", collectorNumber = "64")
@CardRegistration(set = "TMP", collectorNumber = "57")
@CardRegistration(set = "TPR", collectorNumber = "43")
@CardRegistration(set = "S99", collectorNumber = "34")
@CardRegistration(set = "MMQ", collectorNumber = "69")
@CardRegistration(set = "BRB", collectorNumber = "15")
@CardRegistration(set = "S00", collectorNumber = "12")
@CardRegistration(set = "BTD", collectorNumber = "6")
@CardRegistration(set = "SUM", collectorNumber = "54")
@CardRegistration(set = "DD2", collectorNumber = "24")
@CardRegistration(set = "ME2", collectorNumber = "44")
@CardRegistration(set = "EMA", collectorNumber = "43")
@CardRegistration(set = "VMA", collectorNumber = "64")
@CardRegistration(set = "JVC", collectorNumber = "24")
@CardRegistration(set = "MP2", collectorNumber = "10")
@CardRegistration(set = "A25", collectorNumber = "50")
@CardRegistration(set = "SS1", collectorNumber = "4")
@CardRegistration(set = "SLD", collectorNumber = "175")
@CardRegistration(set = "SLD", collectorNumber = "331")
@CardRegistration(set = "SLD", collectorNumber = "1589")
@CardRegistration(set = "SLD", collectorNumber = "1933")
@CardRegistration(set = "SLD", collectorNumber = "2497")
@CardRegistration(set = "STA", collectorNumber = "15")
@CardRegistration(set = "DMR", collectorNumber = "45")
@CardRegistration(set = "GN3", collectorNumber = "25")
@CardRegistration(set = "MAR", collectorNumber = "9")
@CardRegistration(set = "MAR", collectorNumber = "52")
@CardRegistration(set = "FCA", collectorNumber = "4")
@CardRegistration(set = "OMB", collectorNumber = "9")
@CardRegistration(set = "MH2", collectorNumber = "267")
@CardRegistration(set = "MH2", collectorNumber = "308")
@CardRegistration(set = "ME4", collectorNumber = "45")
@CardRegistration(set = "CMM", collectorNumber = "630")
@CardRegistration(set = "CMM", collectorNumber = "81")
public class Counterspell extends Card {

    public Counterspell() {
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
