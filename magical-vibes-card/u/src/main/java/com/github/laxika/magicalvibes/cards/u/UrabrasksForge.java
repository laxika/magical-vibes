package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountersOnSource;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatedPermanentsAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "ONE", collectorNumber = "153")
public class UrabrasksForge extends Card {

    public UrabrasksForge() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED, SequenceEffect.of(
                new PutCountersOnSelfEffect(CounterType.OIL),
                new CreateTokenEffect(
                        CardType.CREATURE,
                        new Fixed(1),
                        "Phyrexian Horror",
                        new CountersOnSource(CounterType.OIL),
                        new Fixed(1),
                        CardColor.RED,
                        null,
                        List.of(CardSubtype.PHYREXIAN, CardSubtype.HORROR),
                        Set.of(Keyword.TRAMPLE, Keyword.HASTE),
                        Set.of(),
                        false,
                        false,
                        Map.of(),
                        List.of(),
                        false,
                        false,
                        false,
                        0,
                        Set.of()),
                new SacrificeCreatedPermanentsAtEndStepEffect()));
    }
}
