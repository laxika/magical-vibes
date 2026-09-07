package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.EnchantedPermanentMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DoesntUntapEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "AFR", collectorNumber = "68")
public class RayOfFrost extends Card {

    public RayOfFrost() {
        PermanentColorInPredicate red = new PermanentColorInPredicate(Set.of(CardColor.RED));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ConditionalEffect(
                        new EnchantedPermanentMatches(red, "enchanted creature is red"),
                        new TapPermanentsEffect(TapUntapScope.TARGET)))
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        red,
                        new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE),
                        null))
                .addEffect(EffectSlot.STATIC, DoesntUntapEffect.enchanted());
    }
}
