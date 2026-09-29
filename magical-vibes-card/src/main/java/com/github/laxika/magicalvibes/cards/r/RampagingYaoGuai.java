package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentsWithinTotalManaValueEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "PIP", collectorNumber = "82")
@CardRegistration(set = "PIP", collectorNumber = "401")
@CardRegistration(set = "PIP", collectorNumber = "610")
@CardRegistration(set = "PIP", collectorNumber = "929")
public class RampagingYaoGuai extends Card {

    public RampagingYaoGuai() {
        PermanentPredicate artifactOrEnchantment = new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(),
                new PermanentIsEnchantmentPredicate()));

        target(new PermanentPredicateTargetFilter(
                artifactOrEnchantment,
                "Target must be an artifact or enchantment"), 0, 99)
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        DestroyTargetPermanentsWithinTotalManaValueEffect.withinTotalManaValue(
                                artifactOrEnchantment, new XValue()));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new EnterWithCountersEffect(CounterType.PLUS_ONE_PLUS_ONE, new XValue()));
    }
}
