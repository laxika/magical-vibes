package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetPlayerDiscardsEffect;

@CardRegistration(set = "TMP", collectorNumber = "125")
public class DauthiMindripper extends Card {

    public DauthiMindripper() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it.
        // If you do, defending player discards three cards.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new SacrificeSelfAndTargetPlayerDiscardsEffect(3),
                        "You may sacrifice it. If you do, defending player discards three cards."));
    }
}
