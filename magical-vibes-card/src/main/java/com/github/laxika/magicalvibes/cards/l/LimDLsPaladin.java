package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeUnlessDiscardCardTypeEffect;

@CardRegistration(set = "ALL", collectorNumber = "108")
public class LimDLsPaladin extends Card {

    public LimDLsPaladin() {
        // At the beginning of your upkeep, you may discard a card. If you don't, sacrifice
        // this creature and draw a card.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new SacrificeUnlessDiscardCardTypeEffect(null, false, true));

        // Whenever this creature becomes blocked, it gets +6/+3 until end of turn.
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BoostSelfEffect(6, 3));

        // Whenever this creature attacks and isn't blocked, it assigns no combat damage this
        // turn and defending player loses 4 life. The defending player is the trigger's
        // non-targeting targetId.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new AssignNoCombatDamageEffect());
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new LoseLifeEffect(4, LoseLifeRecipient.TARGET_PLAYER));
    }
}
