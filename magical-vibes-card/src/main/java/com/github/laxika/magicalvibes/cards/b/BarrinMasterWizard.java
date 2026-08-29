package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "USG", collectorNumber = "63")
public class BarrinMasterWizard extends Card {

    public BarrinMasterWizard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SacrificePermanentCost(new PermanentTruePredicate(), "a permanent", false),
                        ReturnToHandEffect.target()
                ),
                "{2}, Sacrifice a permanent: Return target creature to its owner's hand.",
                TargetFilters.creature()
        ));
    }
}
