package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MakeCreatureUnblockableEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "AVR", collectorNumber = "56")
public class Ghostform extends Card {

    public Ghostform() {
        target(TargetFilters.creature(), 0, 2).addEffect(EffectSlot.SPELL, new MakeCreatureUnblockableEffect());
    }
}
