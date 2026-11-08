package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CSP", collectorNumber = "115")
@CardRegistration(set = "DDM", collectorNumber = "57")
@CardRegistration(set = "C15", collectorNumber = "194")
public class OhranViper extends Card {

    public OhranViper() {
        // Whenever this creature deals combat damage to a creature, destroy that creature at end of combat.
        addTrigger(Triggers.dealsDamageToCreature().withCombatDamageOnly(),
                DestroyTargetPermanentAtEndOfCombatEffect.ofTriggeringPermanent());

        // Whenever this creature deals combat damage to a player, you may draw a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));
    }
}
