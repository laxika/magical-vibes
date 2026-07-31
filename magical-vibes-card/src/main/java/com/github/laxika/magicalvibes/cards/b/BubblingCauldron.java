package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "M14", collectorNumber = "205")
public class BubblingCauldron extends Card {

    public BubblingCauldron() {
        // {1}, {T}, Sacrifice a creature: You gain 4 life.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(new SacrificeCreatureCost(), new GainLifeEffect(4)),
                "{1}, {T}, Sacrifice a creature: You gain 4 life."
        ));

        // {1}, {T}, Sacrifice a creature named Festering Newt: Each opponent loses 4 life.
        // You gain life equal to the life lost this way.
        addActivatedAbility(new ActivatedAbility(
                true, "{1}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentNamedPredicate("Festering Newt"),
                                "Sacrifice a creature named Festering Newt",
                                false
                        ),
                        new LoseLifeEffect(4, LoseLifeRecipient.EACH_OPPONENT, true)
                ),
                "{1}, {T}, Sacrifice a creature named Festering Newt: Each opponent loses 4 life. "
                        + "You gain life equal to the life lost this way."
        ));
    }
}
