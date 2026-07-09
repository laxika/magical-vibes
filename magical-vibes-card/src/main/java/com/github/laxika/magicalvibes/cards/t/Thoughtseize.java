package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "145")
public class Thoughtseize extends Card {

    public Thoughtseize() {
        // Target player reveals their hand; you choose a nonland card; that player discards it.
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), HandChoiceDestination.DISCARD));
        // You lose 2 life.
        addEffect(EffectSlot.SPELL, new LoseLifeEffect(2, LoseLifeRecipient.CONTROLLER));
    }
}
