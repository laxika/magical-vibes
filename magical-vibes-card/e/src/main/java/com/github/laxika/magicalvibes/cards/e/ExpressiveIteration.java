package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsHandBottomExileEffect;

@CardRegistration(set = "STX", collectorNumber = "186")
@CardRegistration(set = "SLD", collectorNumber = "2060")
@CardRegistration(set = "SLD", collectorNumber = "2305")
@CardRegistration(set = "SPG", collectorNumber = "43")
@CardRegistration(set = "SOA", collectorNumber = "64")
@CardRegistration(set = "MB2", collectorNumber = "82")
@CardRegistration(set = "SOC", collectorNumber = "309")
@CardRegistration(set = "MSC", collectorNumber = "183")
@CardRegistration(set = "TDC", collectorNumber = "288")
@CardRegistration(set = "OTC", collectorNumber = "224")
@CardRegistration(set = "BRC", collectorNumber = "125")
public class ExpressiveIteration extends Card {

    public ExpressiveIteration() {
        addEffect(EffectSlot.SPELL, new LookAtTopCardsHandBottomExileEffect(3));
    }
}
