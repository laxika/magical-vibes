package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceThenEffect;

import java.util.List;

@CardRegistration(set = "NEO", collectorNumber = "88")
public class BitingPalmNinja extends Card {

    public BitingPalmNinja() {
        addNinjutsu("{2}{B}");

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.MENACE, new Fixed(1)));

        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new RemoveCounterFromSourceThenEffect(
                                CounterType.MENACE,
                                new ChooseCardsFromTargetHandEffect(
                                        1, List.of(CardType.LAND), List.of(),
                                        HandChoiceDestination.EXILE, false)),
                        "Remove a menace counter from this creature?"));
    }
}
