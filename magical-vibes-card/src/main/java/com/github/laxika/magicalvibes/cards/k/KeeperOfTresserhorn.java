package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "ALL", collectorNumber = "52")
public class KeeperOfTresserhorn extends Card {

    public KeeperOfTresserhorn() {
        // Whenever this creature attacks and isn't blocked, it assigns no combat damage this
        // turn and defending player loses 2 life. The trigger is non-targeting; the defending
        // player is baked in as the stack entry's targetId.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new AssignNoCombatDamageEffect());
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED, new LoseLifeEffect(2, LoseLifeRecipient.TARGET_PLAYER));
    }
}
