package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.TapOrUntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BOK", collectorNumber = "55")
public class TeardropKami extends Card {

    public TeardropKami() {
        // Sacrifice this creature: You may tap or untap target creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new TapOrUntapTargetPermanentEffect()),
                "Sacrifice Teardrop Kami: You may tap or untap target creature.",
                TargetFilters.creature()
        ));
    }
}
