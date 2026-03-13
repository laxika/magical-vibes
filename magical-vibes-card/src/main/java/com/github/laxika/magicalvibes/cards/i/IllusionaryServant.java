package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;

@CardRegistration(set = "M10", collectorNumber = "57")
public class IllusionaryServant extends Card {

    public IllusionaryServant() {
        // When this creature becomes the target of a spell or ability, sacrifice it.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, new SacrificeSelfEffect());
    }
}
