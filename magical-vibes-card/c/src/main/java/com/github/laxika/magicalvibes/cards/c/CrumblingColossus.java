package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M12", collectorNumber = "204")
public class CrumblingColossus extends Card {

    public CrumblingColossus() {
        addTrigger(Triggers.attacks(), new SacrificeAtEndOfCombatEffect());
    }
}
