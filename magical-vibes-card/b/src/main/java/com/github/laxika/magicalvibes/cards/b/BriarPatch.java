package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MMQ", collectorNumber = "232")
public class BriarPatch extends Card {

    public BriarPatch() {
        addTrigger(Triggers.creatureAttacksYou(), new BoostTargetCreatureEffect(-1, 0));
    }
}
