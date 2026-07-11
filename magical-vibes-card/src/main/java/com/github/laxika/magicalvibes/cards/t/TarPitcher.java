package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "LRW", collectorNumber = "193")
public class TarPitcher extends Card {

    public TarPitcher() {
        // {T}, Sacrifice a Goblin: This creature deals 2 damage to any target.
        // Tar Pitcher is itself a Goblin, so it may be sacrificed to its own ability (excludeSource = false).
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificePermanentCost(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.GOBLIN)
                        )),
                        "Sacrifice a Goblin",
                        false
                ), new DealDamageToAnyTargetEffect(2)),
                "{T}, Sacrifice a Goblin: Tar Pitcher deals 2 damage to any target."
        ));
    }
}
