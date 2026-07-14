package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.cards.CardRegistration;

import java.util.List;

@CardRegistration(set = "10E", collectorNumber = "126")
@CardRegistration(set = "7ED", collectorNumber = "117")
@CardRegistration(set = "6ED", collectorNumber = "111")
public class AgonizingMemories extends Card {

    public AgonizingMemories() {
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(2, List.of(), HandChoiceDestination.TOP_OF_LIBRARY));
    }
}
