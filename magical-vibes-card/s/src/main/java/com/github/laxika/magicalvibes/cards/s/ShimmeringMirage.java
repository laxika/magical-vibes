package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GrantBasicLandTypeToTargetEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "APC", collectorNumber = "30")
public class ShimmeringMirage extends Card {

    public ShimmeringMirage() {
        target(TargetFilters.land()).addSpellEffect(new GrantBasicLandTypeToTargetEffect(Duration.UNTIL_END_OF_TURN, null, true));
        addSpellEffect(new DrawCardEffect());
    }
}
