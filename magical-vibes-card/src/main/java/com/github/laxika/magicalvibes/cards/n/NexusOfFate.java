package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleIntoLibraryReplacementEffect;

@CardRegistration(set = "SLD", collectorNumber = "1498")
public class NexusOfFate extends Card {

    public NexusOfFate() {
        addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        addEffect(EffectSlot.STATIC, new ShuffleIntoLibraryReplacementEffect());
    }
}
