package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByMonarchPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "CMM", collectorNumber = "93")
public class FallFromFavor extends Card {

    public FallFromFavor() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new TapPermanentsEffect(TapUntapScope.ENCHANTED))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentControlledByMonarchPredicate(),
                        null,
                        DoesntUntapEffect.enchanted()));
    }
}
