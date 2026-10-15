package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;

@CardRegistration(set = "ISD", collectorNumber = "9")
@CardRegistration(set = "DDQ", collectorNumber = "6")
@CardRegistration(set = "LTC", collectorNumber = "165")
@CardRegistration(set = "C20", collectorNumber = "84")
@CardRegistration(set = "MIC", collectorNumber = "84")
public class DearlyDeparted extends Card {

    public DearlyDeparted() {
        addEffect(EffectSlot.STATIC, new GraveyardEnterWithAdditionalCountersEffect(CardSubtype.HUMAN, 1));
    }
}
