package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;

@CardRegistration(set = "CSP", collectorNumber = "46")
public class RuneSnag extends Card {

    public RuneSnag() {
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(new Sum(
                new Fixed(2),
                new Scaled(new CardsInGraveyard(
                        new CardNamedPredicate("Rune Snag"), CountScope.ANY_PLAYER), 2))));
    }
}
