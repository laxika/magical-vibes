package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MOM", collectorNumber = "79")
public class StasisField extends Card {

    public StasisField() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new SetBasePowerToughnessEffect(0, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
