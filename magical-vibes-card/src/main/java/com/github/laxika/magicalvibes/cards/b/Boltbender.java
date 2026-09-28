package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffect;

@CardRegistration(set = "MKC", collectorNumber = "30")
@CardRegistration(set = "MKC", collectorNumber = "340")
public class Boltbender extends Card {

    public Boltbender() {
        addMorph("{1}{R}");
        addEffect(EffectSlot.ON_TURNED_FACE_UP,
                new ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffect());
    }
}
