package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "5ED", collectorNumber = "268")
public class Smoke extends Card {

    public Smoke() {
        // Static: players can't untap more than one creature during their untap steps. The untap
        // step pauses to let the active player pick which single creature untaps; non-creatures
        // untap normally.
        addEffect(EffectSlot.STATIC, new StaticOrbEffect(1, new PermanentIsCreaturePredicate(), false));
    }
}
