package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;

import java.util.List;

@CardRegistration(set = "MH2", collectorNumber = "163")
public class GlimmerBairn extends Card {

    public GlimmerBairn() {
        // Sacrifice a token: This creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsTokenPredicate(), "a token"),
                        new BoostSelfEffect(2, 2)
                ),
                "Sacrifice a token: This creature gets +2/+2 until end of turn."
        ));
    }
}
