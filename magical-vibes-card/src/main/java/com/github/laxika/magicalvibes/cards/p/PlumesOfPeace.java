package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DIS", collectorNumber = "124")
public class PlumesOfPeace extends Card {

    public PlumesOfPeace() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());

        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{W}{U}",
                List.of(new TapPermanentsEffect(TapUntapScope.TARGET)),
                "Forecast \u2014 {W}{U}, Reveal this card from your hand: Tap target creature. "
                        + "Activate only during your upkeep and only once each turn.",
                TargetFilters.creature(),
                null,
                1,
                ActivationTimingRestriction.ONLY_DURING_YOUR_UPKEEP
        ).withRevealsSourceFromHand());
    }
}
