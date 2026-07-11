package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllyCombatDamageTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;

@CardRegistration(set = "MOR", collectorNumber = "72")
public class OonasBlackguard extends Card {

    public OonasBlackguard() {
        // Each other Rogue creature you control enters with an additional +1/+1 counter on it.
        addEffect(EffectSlot.STATIC, new ControlledCreaturesEnterWithAdditionalCountersEffect(CardSubtype.ROGUE, 1));

        // Whenever a creature you control with a +1/+1 counter on it deals combat damage to a
        // player, that player discards a card.
        addEffect(EffectSlot.ON_ALLY_CREATURE_COMBAT_DAMAGE_TO_PLAYER,
                new AllyCombatDamageTriggerEffect(
                        new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER)));
    }
}
