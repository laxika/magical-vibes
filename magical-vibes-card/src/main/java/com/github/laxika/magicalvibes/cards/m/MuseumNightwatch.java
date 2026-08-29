package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.SourceIsFaceDown;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "MKM", collectorNumber = "25")
public class MuseumNightwatch extends Card {

    public MuseumNightwatch() {
        addMorph("{1}{W}");
        addEffect(EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                new ConditionalEffect(new SourceIsFaceDown(), new CounterUnlessPaysEffect(2)));
        addEffect(EffectSlot.ON_DEATH, new CreateTokenEffect(
                1,
                "Detective",
                2,
                2,
                CardColor.WHITE,
                Set.of(CardColor.WHITE, CardColor.BLUE),
                List.of(CardSubtype.DETECTIVE)));
    }
}
