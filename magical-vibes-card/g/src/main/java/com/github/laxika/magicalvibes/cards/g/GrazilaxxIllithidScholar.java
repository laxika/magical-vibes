package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "AFR", collectorNumber = "60")
@CardRegistration(set = "SLD", collectorNumber = "956")
@CardRegistration(set = "SLD", collectorNumber = "1928")
public class GrazilaxxIllithidScholar extends Card {

    public GrazilaxxIllithidScholar() {
        // Whenever a creature you control becomes blocked, you may return it to its owner's hand.
        addTrigger(Triggers.creatureYouControlBecomesBlocked(),
                new MayEffect(ReturnToHandEffect.self(), "Return that creature to its owner's hand?"));

        // Whenever one or more creatures you control deal combat damage to a player, draw a card.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new AllyCombatDamageTriggerEffect(null, new DrawCardEffect(1), false, true));
    }
}
