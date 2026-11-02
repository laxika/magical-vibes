package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllowCastForetoldCardThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantForetellToNonlandCardsInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "598")
public class SingingTowersOfDarillium extends Card {

    public SingingTowersOfDarillium() {
        addStaticEffect(new GrantForetellToNonlandCardsInHandEffect());
        addTrigger(Triggers.chaosEnsues(), new AllowCastForetoldCardThisTurnEffect());
    }
}
