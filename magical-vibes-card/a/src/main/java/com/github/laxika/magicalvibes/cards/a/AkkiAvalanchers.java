package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "151")
public class AkkiAvalanchers extends Card {

    public AkkiAvalanchers() {
        // Sacrifice a land: This creature gets +2/+0 until end of turn. Activate only once each turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsLandPredicate(), "Sacrifice a land", false),
                        new BoostSelfEffect(2, 0)),
                "Sacrifice a land: This creature gets +2/+0 until end of turn. Activate only once each turn.",
                1));
    }
}
