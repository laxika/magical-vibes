package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "9ED", collectorNumber = "70")
@CardRegistration(set = "8ED", collectorNumber = "71")
public class Cowardice extends Card {

    public Cowardice() {
        // Whenever a creature becomes the target of a spell or ability, return that creature
        // to its owner's hand. The targeted creature is set as the non-targeting targetId.
        addEffect(EffectSlot.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
                ReturnToHandEffect.target());
    }
}
