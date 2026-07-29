package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "MIR", collectorNumber = "143")
public class SkulkingGhost extends Card {

    public SkulkingGhost() {
        // When this creature becomes the target of a spell or ability, sacrifice it.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new SacrificeSelfEffect());
    }
}
