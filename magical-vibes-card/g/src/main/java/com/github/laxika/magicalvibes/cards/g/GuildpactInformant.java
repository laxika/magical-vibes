package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WAR", collectorNumber = "271")
public class GuildpactInformant extends Card {

    public GuildpactInformant() {
        addTrigger(Triggers.dealsCombatDamageToPlayerOrPlaneswalker(),
                new ProliferateEffect());
    }
}
