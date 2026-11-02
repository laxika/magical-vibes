package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnotherCreatureDrawAndMayPutPermanentEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class VenomLethalProtector extends Card {

    public VenomLethalProtector() {
        addTrigger(Triggers.attacks(), new MayEffect(
                new SacrificeAnotherCreatureDrawAndMayPutPermanentEffect(),
                "You may sacrifice another creature."));
    }
}
