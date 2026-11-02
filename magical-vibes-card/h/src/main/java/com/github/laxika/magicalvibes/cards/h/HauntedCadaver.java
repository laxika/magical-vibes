package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONS", collectorNumber = "154")
public class HauntedCadaver extends Card {

    public HauntedCadaver() {
        addMorph("{1}{B}");
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new SacrificeSelfThenEffect(
                        new DiscardEffect(3, DiscardRecipient.TRIGGERING_PLAYER)),
                        "You may sacrifice it. If you do, that player discards three cards."));
    }
}
