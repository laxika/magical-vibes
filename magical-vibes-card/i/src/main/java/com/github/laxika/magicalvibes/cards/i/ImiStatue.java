package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.StaticOrbEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

@CardRegistration(set = "CHK", collectorNumber = "255")
public class ImiStatue extends Card {

    public ImiStatue() {
        // Static: players can't untap more than one artifact during their untap steps. The lock has
        // no "as long as this is untapped" clause, so it applies while this artifact is on the
        // battlefield in any state; non-artifact permanents untap normally.
        addEffect(EffectSlot.STATIC, new StaticOrbEffect(1, new PermanentIsArtifactPredicate(), false));
    }
}
