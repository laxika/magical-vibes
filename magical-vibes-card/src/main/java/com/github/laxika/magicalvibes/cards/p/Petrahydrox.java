package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "GPT", collectorNumber = "148")
public class Petrahydrox extends Card {

    public Petrahydrox() {
        // When this creature becomes the target of a spell or ability, return it to its owner's hand.
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY, ReturnToHandEffect.self());
    }
}
