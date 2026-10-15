package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "135")
public class Extortion extends Card {

    public Extortion() {
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                new Fixed(2), List.of(), List.of(), HandChoiceDestination.DISCARD,
                false, null, 0, true, false, false, false));
    }
}
