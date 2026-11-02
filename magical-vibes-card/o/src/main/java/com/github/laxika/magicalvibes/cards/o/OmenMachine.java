package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OmenMachineDrawStepEffect;
import com.github.laxika.magicalvibes.model.effect.PlayersCannotDrawCardsEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NPH", collectorNumber = "148")
public class OmenMachine extends Card {

    public OmenMachine() {
        addStaticEffect(new PlayersCannotDrawCardsEffect());
        addTrigger(Triggers.drawStep(ActivePlayer.ANY), new OmenMachineDrawStepEffect());
    }
}
