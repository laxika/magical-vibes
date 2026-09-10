package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnSelfThenTransformIfThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;

import java.util.List;
import java.util.Set;

public class EdgarMarkovsCoffin extends Card {

    public EdgarMarkovsCoffin() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, SequenceEffect.of(
                new CreateTokenEffect(
                        1, "Vampire", 1, 1, CardColor.WHITE,
                        Set.of(CardColor.WHITE, CardColor.BLACK),
                        List.of(CardSubtype.VAMPIRE), Set.of(Keyword.LIFELINK), Set.of()),
                new PutCounterOnSelfThenTransformIfThresholdEffect(CounterType.BLOODLINE, 3)));
    }
}
