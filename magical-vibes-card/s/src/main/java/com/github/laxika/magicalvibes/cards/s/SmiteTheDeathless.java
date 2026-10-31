package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "HOC", collectorNumber = "202")
public class SmiteTheDeathless extends Card {

    public SmiteTheDeathless() {
        target(TargetFilters.creature()).addSpellEffect(new DealDamageToAnyTargetThenRemoveKeywordIfDamagedEffect(
                        new Fixed(3), Keyword.INDESTRUCTIBLE, true));
    }
}
