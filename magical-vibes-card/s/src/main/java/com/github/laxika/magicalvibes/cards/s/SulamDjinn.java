package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "INV", collectorNumber = "212")
public class SulamDjinn extends Card {

    public SulamDjinn() {
        addStaticEffect(new ConditionalEffect(
                new ColorMostCommonAmongAllPermanents(CardColor.GREEN),
                new StaticBoostEffect(-2, -2, Subjects.source())));
    }
}
