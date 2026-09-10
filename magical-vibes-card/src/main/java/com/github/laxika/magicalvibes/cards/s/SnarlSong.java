package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateXTokenWithXCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "SOS", collectorNumber = "161")
public class SnarlSong extends Card {

    public SnarlSong() {
        // Converge creates two 0/0 green and blue Fractal creature tokens and puts X +1/+1
        // counters on each of them, where X is the number of colors spent to cast this spell.
        CreateXTokenWithXCountersEffect fractal = new CreateXTokenWithXCountersEffect(
                "Fractal", 0, 0,
                CardColor.GREEN, Set.of(CardColor.GREEN, CardColor.BLUE),
                List.of(CardSubtype.FRACTAL), CounterType.PLUS_ONE_PLUS_ONE);
        addEffect(EffectSlot.SPELL, fractal);
        addEffect(EffectSlot.SPELL, fractal);
        addEffect(EffectSlot.SPELL, new GainLifeEffect(new XValue()));
    }
}
