package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.OncePerTurnTriggerEffect;

@CardRegistration(set = "INR", collectorNumber = "124")
@CardRegistration(set = "INR", collectorNumber = "388")
@CardRegistration(set = "MID", collectorNumber = "113")
@CardRegistration(set = "SPG", collectorNumber = "32")
@CardRegistration(set = "SOC", collectorNumber = "219")
@CardRegistration(set = "DBL", collectorNumber = "113")
@CardRegistration(set = "PIP", collectorNumber = "186")
@CardRegistration(set = "PIP", collectorNumber = "714")
@CardRegistration(set = "FIC", collectorNumber = "278")
@CardRegistration(set = "DSC", collectorNumber = "148")
@CardRegistration(set = "TDC", collectorNumber = "188")
public class MorbidOpportunist extends Card {

    public MorbidOpportunist() {
        // Whenever one or more other creatures die, draw a card. This ability triggers only once each turn.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES,
                new OncePerTurnTriggerEffect(new DrawCardEffect(1)));
    }
}
