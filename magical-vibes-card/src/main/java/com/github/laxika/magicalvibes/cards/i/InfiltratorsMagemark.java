package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "28")
public class InfiltratorsMagemark extends Card {

    public InfiltratorsMagemark() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, new PermanentIsEnchantedPredicate()))
                .addEffect(EffectSlot.STATIC, new GrantEffectEffect(
                        new CanBeBlockedOnlyByFilterEffect(
                                new PermanentHasKeywordPredicate(Keyword.DEFENDER),
                                "creatures with defender"),
                        GrantScope.ALL_OWN_CREATURES,
                        new PermanentIsEnchantedPredicate()));
    }
}
