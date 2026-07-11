package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PreventNextDamageFromChosenSourceToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;

import java.util.List;

@CardRegistration(set = "9ED", collectorNumber = "40")
public class SanctumGuardian extends Card {

    public SanctumGuardian() {
        // Sacrifice this creature: The next time a source of your choice would deal damage to any
        // target this turn, prevent that damage.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeSelfCost(),
                        new PreventNextDamageFromChosenSourceToAnyTargetEffect()
                ),
                "Sacrifice this creature: The next time a source of your choice would deal damage to any target this turn, prevent that damage."
        ));
    }
}
