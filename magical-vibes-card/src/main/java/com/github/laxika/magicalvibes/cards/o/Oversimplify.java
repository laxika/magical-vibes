package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOC", collectorNumber = "322")
public class Oversimplify extends Card {

    public Oversimplify() {
        addEffect(EffectSlot.SPELL, new ExileAllCreaturesAndCreateTokenWithCountersEqualToTotalPowerEffect(
                new CreateTokenEffect("Fractal", 0, 0, CardColor.GREEN,
                        Set.of(CardColor.GREEN, CardColor.BLUE), List.of(CardSubtype.FRACTAL)),
                CounterType.PLUS_ONE_PLUS_ONE));
    }
}
