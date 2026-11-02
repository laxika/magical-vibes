package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "210")
public class CorpseberryCultivator extends Card {

    public CorpseberryCultivator() {
        addTrigger(Triggers.beginningOfCombat(ActivePlayer.YOU),
                new MayEffect(new ForageEffect(), "Forage?"));
        addTrigger(Triggers.youForage(),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
