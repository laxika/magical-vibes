package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "6ED", collectorNumber = "130")
@CardRegistration(set = "HML", collectorNumber = "47a")
@CardRegistration(set = "HML", collectorNumber = "47b")
@CardRegistration(set = "ATH", collectorNumber = "22")
public class FeastOfTheUnicorn extends Card {

    public FeastOfTheUnicorn() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(4, 0, GrantScope.ENCHANTED_CREATURE));
    }
}
