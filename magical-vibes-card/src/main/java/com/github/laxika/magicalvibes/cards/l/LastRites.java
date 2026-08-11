package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardAnyNumberEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "146")
public class LastRites extends Card {

    public LastRites() {
        addEffect(EffectSlot.SPELL, new DiscardAnyNumberEffect());
        addEffect(EffectSlot.SPELL, new ChooseCardsFromTargetHandEffect(
                new EventValue(), List.of(CardType.LAND), HandChoiceDestination.DISCARD));
    }
}
