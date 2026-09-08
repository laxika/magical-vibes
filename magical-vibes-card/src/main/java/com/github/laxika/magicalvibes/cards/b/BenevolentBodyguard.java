package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "JUD", collectorNumber = "5")
public class BenevolentBodyguard extends Card {

    public BenevolentBodyguard() {
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new GrantProtectionChoiceUntilEndOfTurnEffect()),
                "Sacrifice this creature: Target creature you control gains protection from the color of your choice until end of turn.",
                TargetFilters.creatureYouControl()
        ));
    }
}
