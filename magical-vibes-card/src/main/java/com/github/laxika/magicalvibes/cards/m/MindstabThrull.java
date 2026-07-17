package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfAndTargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "178")
public class MindstabThrull extends Card {

    public MindstabThrull() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it.
        // If you do, defending player discards three cards.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new SacrificeSelfAndTargetPlayerDiscardsEffect(3),
                        "You may sacrifice it. If you do, defending player discards three cards."));
    }
}
