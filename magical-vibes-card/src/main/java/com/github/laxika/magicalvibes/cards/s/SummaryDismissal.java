package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileOtherSpellsAndCounterAbilitiesEffect;

@CardRegistration(set = "INR", collectorNumber = "88")
public class SummaryDismissal extends Card {

    public SummaryDismissal() {
        // Exile all other spells and counter all abilities.
        addEffect(EffectSlot.SPELL, new ExileOtherSpellsAndCounterAbilitiesEffect());
    }
}
