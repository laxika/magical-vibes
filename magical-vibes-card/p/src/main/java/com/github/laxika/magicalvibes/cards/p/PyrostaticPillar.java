package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SCG", collectorNumber = "100")
public class PyrostaticPillar extends Card {

    public PyrostaticPillar() {
        addTrigger(Triggers.playerCastsSpell().withStackEntryFilter(new StackEntryMaxManaValuePredicate(3)),
                new DealDamageToPlayersEffect(2, DamageRecipient.TRIGGERING_PLAYER));
    }
}
