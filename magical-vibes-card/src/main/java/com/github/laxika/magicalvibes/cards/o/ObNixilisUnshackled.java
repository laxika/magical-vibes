package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "M15", collectorNumber = "110")
public class ObNixilisUnshackled extends Card {

    public ObNixilisUnshackled() {
        // Whenever an opponent searches their library, that player sacrifices a creature of their
        // choice and loses 10 life. The searching player is baked in as the trigger's targetId, so
        // both TARGET_PLAYER-scoped effects act on them.
        addEffect(EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY, new SacrificePermanentsEffect(
                1, new PermanentIsCreaturePredicate(), SacrificeRecipient.TARGET_PLAYER));
        addEffect(EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY,
                new LoseLifeEffect(10, LoseLifeRecipient.TARGET_PLAYER));

        // Whenever another creature dies, put a +1/+1 counter on Ob Nixilis.
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new PutCountersOnSourceEffect(1, 1, 1));
    }
}
