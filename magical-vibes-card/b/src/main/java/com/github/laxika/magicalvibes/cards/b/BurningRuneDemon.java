package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BurningRuneDemonEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "81")
@CardRegistration(set = "KHM", collectorNumber = "349")
public class BurningRuneDemon extends Card {

    public BurningRuneDemon() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MayEffect(
                new BurningRuneDemonEffect(),
                "Search your library for exactly two cards with different names?"));
    }
}
