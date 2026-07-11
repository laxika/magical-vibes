package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.condition.Morbid;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;

import java.util.List;

@CardRegistration(set = "DKA", collectorNumber = "66")
public class GruesomeDiscovery extends Card {

    public GruesomeDiscovery() {
        addEffect(EffectSlot.SPELL, new ConditionalReplacementEffect(new Morbid(), 
                new DiscardEffect(2, DiscardRecipient.TARGET_PLAYER),
                new ChooseCardsFromTargetHandEffect(2, List.of(), HandChoiceDestination.DISCARD)
        ));
    }
}
