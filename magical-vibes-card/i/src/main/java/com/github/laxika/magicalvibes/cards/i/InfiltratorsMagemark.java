package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "28")
public class InfiltratorsMagemark extends Card {

    public InfiltratorsMagemark() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, Subjects.otherCreaturesYouControl(), new PermanentIsEnchantedPredicate()))
                .addStaticEffect(new GrantEffectEffect(
                        new CanBeBlockedOnlyByFilterEffect(
                                new PermanentHasKeywordPredicate(Keyword.DEFENDER),
                                "creatures with defender"),
                        Subjects.creaturesYouControl(),
                        new PermanentIsEnchantedPredicate()));
    }
}
