package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "8")
public class GuardiansMagemark extends Card {

    public GuardiansMagemark() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(), new PermanentIsEnchantedPredicate()));
    }
}
