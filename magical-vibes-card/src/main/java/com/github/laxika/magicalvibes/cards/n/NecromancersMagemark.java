package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreaturesYouControlToHandInsteadOfDyingEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "53")
public class NecromancersMagemark extends Card {

    public NecromancersMagemark() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                                new PermanentIsEnchantedPredicate()))
                .addEffect(EffectSlot.STATIC,
                        new ReturnEnchantedCreaturesYouControlToHandInsteadOfDyingEffect());
    }
}
