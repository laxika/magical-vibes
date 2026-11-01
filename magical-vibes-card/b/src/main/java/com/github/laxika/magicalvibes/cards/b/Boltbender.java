package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MKC", collectorNumber = "30")
@CardRegistration(set = "MKC", collectorNumber = "340")
public class Boltbender extends Card {

    public Boltbender() {
        addMorph("{1}{R}");
        addTrigger(Triggers.turnedFaceUp(null, ObjectRelation.THIS),
                new ChooseNewTargetsForAnyNumberOfOtherSpellsAndAbilitiesEffect());
    }
}
