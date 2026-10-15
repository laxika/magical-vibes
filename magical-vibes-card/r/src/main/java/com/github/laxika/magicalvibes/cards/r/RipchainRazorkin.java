package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "154")
public class RipchainRazorkin extends Card {

    public RipchainRazorkin() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}{R}",
                List.of(new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land"),
                        new DrawCardEffect()),
                "{2}{R}, Sacrifice a land: Draw a card."
        ));
    }
}
