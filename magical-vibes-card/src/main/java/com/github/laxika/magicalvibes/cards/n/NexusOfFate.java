package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;

@CardRegistration(set = "SPG", collectorNumber = "122")
public class NexusOfFate extends Card {

    public NexusOfFate() {
        // Take an extra turn after this one.
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));

        // If Nexus of Fate would be put into a graveyard from anywhere, reveal it and shuffle it
        // into its owner's library instead.
        addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect());
    }
}
