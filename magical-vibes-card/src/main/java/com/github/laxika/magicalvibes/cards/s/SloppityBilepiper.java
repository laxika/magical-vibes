package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantCascadeToNextCreatureSpellThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "59")
public class SloppityBilepiper extends Card {

    public SloppityBilepiper() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentIsCreaturePredicate(),
                                "a creature",
                                false),
                        new GrantCascadeToNextCreatureSpellThisTurnEffect()),
                "{2}, {T}, Sacrifice a creature: The next creature spell you cast this turn has cascade."
        ));
    }
}
