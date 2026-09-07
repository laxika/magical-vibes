package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

@CardRegistration(set = "GPT", collectorNumber = "60")
public class SanguinePraetor extends Card {

    private static final PermanentPredicate CREATURE_WITH_SAME_MANA_VALUE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentManaValueEqualsXPredicate()
    ));

    public SanguinePraetor() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(
                        new SacrificeCreatureCost(true),
                        new DestroyAllPermanentsEffect(CREATURE_WITH_SAME_MANA_VALUE)
                ),
                "{B}, Sacrifice a creature: Destroy each creature with the same mana value as the sacrificed creature."
        ));
    }
}
