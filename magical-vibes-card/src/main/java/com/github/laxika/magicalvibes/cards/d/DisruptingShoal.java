package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.effect.CounterSpellIfManaValueEqualsXEffect;
import com.github.laxika.magicalvibes.model.filter.CardColorPredicate;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "33")
public class DisruptingShoal extends Card {

    public DisruptingShoal() {
        addEffect(EffectSlot.SPELL, new CounterSpellIfManaValueEqualsXEffect());
        addCastingOption(new AlternateHandCast(List.of(
                ExileCardsFromHandCastingCost.withManaValueX(new CardColorPredicate(CardColor.BLUE), "blue"))));
    }
}
