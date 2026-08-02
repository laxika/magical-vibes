package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantProtectionChoiceUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

@CardRegistration(set = "GTC", collectorNumber = "150")
public class CartelAristocrat extends Card {

    public CartelAristocrat() {
        // Sacrifice another creature: This creature gains protection from the color of your choice until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new SacrificeCreatureCost(false, false, false, true),
                        new GrantProtectionChoiceUntilEndOfTurnEffect(GrantScope.SELF)
                ),
                "Sacrifice another creature: Cartel Aristocrat gains protection from the color of your choice until end of turn."
        ));
    }
}
