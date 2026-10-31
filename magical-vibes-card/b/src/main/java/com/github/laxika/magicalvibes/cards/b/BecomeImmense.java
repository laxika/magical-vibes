package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "130")
@CardRegistration(set = "UMA", collectorNumber = "157")
@CardRegistration(set = "TSR", collectorNumber = "358")
public class BecomeImmense extends Card {

    public BecomeImmense() {
        addCastingCost(new DelveCost());
        target(TargetFilters.creature())
                .addSpellEffect(new BoostTargetCreatureEffect(6, 6));
    }
}
