package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastSpellsWithManaValueAtMostEffect;

@CardRegistration(set = "INR", collectorNumber = "14b")
public class BriselaVoiceOfNightmares extends Card {

    public BriselaVoiceOfNightmares() {
        // Your opponents can't cast spells with mana value 3 or less.
        addEffect(EffectSlot.STATIC, new OpponentsCantCastSpellsWithManaValueAtMostEffect(3));
    }
}
