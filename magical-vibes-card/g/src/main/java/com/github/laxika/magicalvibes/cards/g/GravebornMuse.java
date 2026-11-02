package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DrawAndLoseLifePerSubtypeEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "145")
@CardRegistration(set = "LGN", collectorNumber = "73")
@CardRegistration(set = "FDC", collectorNumber = "103")
public class GravebornMuse extends Card {

    public GravebornMuse() {
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new DrawAndLoseLifePerSubtypeEffect(CardSubtype.ZOMBIE));
    }
}
