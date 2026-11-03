package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCountersFromSourceToOtherCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "120")
@CardRegistration(set = "HOP", collectorNumber = "73")
@CardRegistration(set = "SLD", collectorNumber = "829")
@CardRegistration(set = "SLD", collectorNumber = "1890")
@CardRegistration(set = "DMR", collectorNumber = "161")
@CardRegistration(set = "SOC", collectorNumber = "267")
@CardRegistration(set = "C15", collectorNumber = "185")
@CardRegistration(set = "FIC", collectorNumber = "304")
@CardRegistration(set = "MOC", collectorNumber = "297")
@CardRegistration(set = "C21", collectorNumber = "189")
@CardRegistration(set = "NCC", collectorNumber = "291")
@CardRegistration(set = "BLC", collectorNumber = "217")
@CardRegistration(set = "NEC", collectorNumber = "117")
@CardRegistration(set = "C16", collectorNumber = "150")
@CardRegistration(set = "CM2", collectorNumber = "137")
@CardRegistration(set = "ARC", collectorNumber = "57")
@CardRegistration(set = "E01", collectorNumber = "65")
public class ForgottenAncient extends Card {

    public ForgottenAncient() {
        addTrigger(Triggers.playerCastsSpell().anySpell(),
                new MayEffect(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE), "Put a +1/+1 counter on Forgotten Ancient?"));
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new MayEffect(
                new MoveCountersFromSourceToOtherCreaturesEffect(CounterType.PLUS_ONE_PLUS_ONE),
                "Move any number of +1/+1 counters from Forgotten Ancient onto other creatures?"));
    }
}
