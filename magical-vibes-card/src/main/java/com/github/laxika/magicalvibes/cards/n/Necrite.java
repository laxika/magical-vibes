package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "5ED", collectorNumber = "181")
public class Necrite extends Card {

    public Necrite() {
        // Whenever this creature attacks and isn't blocked, you may sacrifice it.
        // If you do, destroy target creature defending player controls. It can't be regenerated.
        addEffect(EffectSlot.ON_ATTACKS_UNBLOCKED,
                new MayEffect(new SacrificeSelfToDestroyCreatureDamagedPlayerControlsEffect(true),
                        "You may sacrifice it. If you do, destroy target creature defending player controls. "
                                + "It can't be regenerated."));
    }
}
