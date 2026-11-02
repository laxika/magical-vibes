package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentChoosesMasterOfCeremoniesEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "18")
@CardRegistration(set = "NCC", collectorNumber = "119")
@CardRegistration(set = "SLD", collectorNumber = "2379")
public class MasterOfCeremonies extends Card {

    public MasterOfCeremonies() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new EachOpponentChoosesMasterOfCeremoniesEffect());
    }
}
