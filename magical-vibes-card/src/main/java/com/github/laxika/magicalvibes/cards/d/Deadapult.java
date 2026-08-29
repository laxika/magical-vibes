package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "59")
public class Deadapult extends Card {

    public Deadapult() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.ZOMBIE), "a Zombie"),
                        new DealDamageToAnyTargetEffect(2)),
                "{R}, Sacrifice a Zombie: This enchantment deals 2 damage to any target."
        ));
    }
}
