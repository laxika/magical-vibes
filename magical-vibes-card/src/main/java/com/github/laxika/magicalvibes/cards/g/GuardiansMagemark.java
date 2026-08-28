package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "8")
public class GuardiansMagemark extends Card {

    public GuardiansMagemark() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES, new PermanentIsEnchantedPredicate()));
    }
}
