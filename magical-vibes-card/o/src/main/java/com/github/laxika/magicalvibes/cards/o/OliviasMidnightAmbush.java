package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.FixedIfCondition;
import com.github.laxika.magicalvibes.model.condition.IsNight;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MID", collectorNumber = "118")
@CardRegistration(set = "DBL", collectorNumber = "118")
public class OliviasMidnightAmbush extends Card {

    public OliviasMidnightAmbush() {
        target(TargetFilters.creature()).addSpellEffect(BoostEffect.target(
                        new FixedIfCondition(new IsNight(), -13, -2),
                        new FixedIfCondition(new IsNight(), -13, -2)));
    }
}
