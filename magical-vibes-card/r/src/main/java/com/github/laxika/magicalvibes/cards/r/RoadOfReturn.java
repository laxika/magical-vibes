package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.EntwineManaCost;
import com.github.laxika.magicalvibes.model.effect.PutCommanderFromCommandZoneIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "C19", collectorNumber = "34")
public class RoadOfReturn extends Card {

    public RoadOfReturn() {
        addEffect(EffectSlot.SPELL, new EntwineManaCost("{2}"));
        addEffect(EffectSlot.SPELL, new ChooseOneEffect(List.of(
                new ChooseOneEffect.ChooseOneOption(
                        "Return target permanent card from your graveyard to your hand",
                        new ReturnTargetCardsFromGraveyardToHandEffect(
                                new CardIsPermanentPredicate(), 1)),
                new ChooseOneEffect.ChooseOneOption(
                        "Put your commander into your hand from the command zone",
                        new PutCommanderFromCommandZoneIntoHandEffect())
        ), false, 1, 2, true));
    }
}
