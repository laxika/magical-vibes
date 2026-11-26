package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLA", collectorNumber = "130")
public class CunningManeuver extends Card {

    public CunningManeuver() {
        target(TargetFilters.creature())
                .addSpellEffect(BoostEffect.target(3, 1))
                .addSpellEffect(CreateTokenEffect.ofClueToken(1).withInvestigate(false));
    }
}
