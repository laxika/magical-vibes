package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.NoMaximumHandSizeEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOM", collectorNumber = "220")
@CardRegistration(set = "C21", collectorNumber = "273")
@CardRegistration(set = "C16", collectorNumber = "279")
public class VensersJournal extends Card {

    public VensersJournal() {
        addStaticEffect(new NoMaximumHandSizeEffect());
        addTrigger(Triggers.upkeep(ActivePlayer.YOU), new GainLifeEffect(new CardsInHand(CountScope.CONTROLLER)));
    }
}
