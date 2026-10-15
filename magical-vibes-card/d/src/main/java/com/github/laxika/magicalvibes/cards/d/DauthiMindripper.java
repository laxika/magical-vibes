package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;

@CardRegistration(set = "TMP", collectorNumber = "125")
public class DauthiMindripper extends Card {

    public DauthiMindripper() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it.
        // If you do, defending player discards three cards. The defending player is baked onto the
        // trigger's targetId by the unblocked-attack collector, so TARGET_PLAYER reads it.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new SacrificeSelfThenEffect(new DiscardEffect(3, DiscardRecipient.TARGET_PLAYER)),
                        "You may sacrifice it. If you do, defending player discards three cards."));
    }
}
