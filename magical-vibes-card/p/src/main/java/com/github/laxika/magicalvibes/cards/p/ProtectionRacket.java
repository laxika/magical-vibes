package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProtectionRacketEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "39")
@CardRegistration(set = "NCC", collectorNumber = "140")
public class ProtectionRacket extends Card {

    public ProtectionRacket() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new ProtectionRacketEffect());
    }
}
