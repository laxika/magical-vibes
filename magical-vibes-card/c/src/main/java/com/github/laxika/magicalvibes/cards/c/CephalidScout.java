package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "74")
public class CephalidScout extends Card {

    public CephalidScout() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{U}",
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new DrawCardEffect(1)),
                "{2}{U}, Sacrifice a land: Draw a card."
        ));
    }
}
