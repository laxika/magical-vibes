package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "90")
@CardRegistration(set = "PIP", collectorNumber = "406")
@CardRegistration(set = "PIP", collectorNumber = "618")
@CardRegistration(set = "PIP", collectorNumber = "934")
public class AlmostPerfect extends Card {

    public AlmostPerfect() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(9, 10, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.ENCHANTED_CREATURE));
    }
}
