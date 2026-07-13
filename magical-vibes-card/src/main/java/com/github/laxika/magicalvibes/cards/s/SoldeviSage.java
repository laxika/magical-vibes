package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawAndDiscardCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "99")
public class SoldeviSage extends Card {

    public SoldeviSage() {
        // {T}, Sacrifice two lands: Draw three cards, then discard one of them.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate()),
                        new DrawAndDiscardCardEffect(3, 1)),
                "{T}, Sacrifice two lands: Draw three cards, then discard one of them."
        ));
    }
}
