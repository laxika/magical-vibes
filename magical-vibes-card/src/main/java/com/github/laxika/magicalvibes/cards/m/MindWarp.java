package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "143")
@CardRegistration(set = "5ED", collectorNumber = "177")
@CardRegistration(set = "ICE", collectorNumber = "148")
public class MindWarp extends Card {

    public MindWarp() {
        // Look at target player's hand and choose X cards from it; that player discards them.
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                new XValue(), List.of(), HandChoiceDestination.DISCARD));
    }
}
