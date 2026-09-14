package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensWithChosenCountersEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEO", collectorNumber = "193")
public class InvokeTheAncients extends Card {

    public InvokeTheAncients() {
        CreateTokenEffect spirit = new CreateTokenEffect(
                2,
                "Spirit",
                4,
                5,
                CardColor.GREEN,
                List.of(CardSubtype.SPIRIT),
                Set.of(),
                Set.of());

        addEffect(EffectSlot.SPELL, new CreateTokensWithChosenCountersEffect(
                spirit,
                List.of(CounterType.REACH, CounterType.VIGILANCE, CounterType.TRAMPLE)));
    }
}
