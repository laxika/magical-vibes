package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "80")
public class DispersingOrb extends Card {

    public DispersingOrb() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{U}",
                List.of(
                        new SacrificePermanentCost(new PermanentTruePredicate(), "a permanent", false),
                        ReturnToHandEffect.target()
                ),
                "{3}{U}, Sacrifice a permanent: Return target permanent to its owner's hand.",
                TargetFilters.permanent()
        ));
    }
}
