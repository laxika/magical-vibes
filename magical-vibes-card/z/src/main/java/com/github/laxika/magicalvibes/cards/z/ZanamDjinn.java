package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.condition.ColorMostCommonAmongAllPermanents;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "INV", collectorNumber = "90")
public class ZanamDjinn extends Card {

    public ZanamDjinn() {
        addStaticEffect(new ConditionalEffect(
                new ColorMostCommonAmongAllPermanents(CardColor.BLUE),
                new StaticBoostEffect(-2, -2, Subjects.source())));
    }
}
