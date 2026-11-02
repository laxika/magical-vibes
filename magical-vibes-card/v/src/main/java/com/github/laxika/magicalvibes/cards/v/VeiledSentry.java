package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomeCreatureByCastSpellManaValueEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "USG", collectorNumber = "109")
public class VeiledSentry extends Card {

    public VeiledSentry() {
        addTrigger(Triggers.opponentCastsSpell(), new BecomeCreatureByCastSpellManaValueEffect());
    }
}
