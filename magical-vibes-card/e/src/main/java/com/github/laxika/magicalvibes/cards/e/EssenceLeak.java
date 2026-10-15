package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PayManaCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "INV", collectorNumber = "55")
public class EssenceLeak extends Card {

    public EssenceLeak() {
        target(TargetFilters.permanent())
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.RED, CardColor.GREEN)),
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.UPKEEP_TRIGGERED,
                                new ForcedCostOrElseEffect(
                                        PayManaCost.forSourceManaCost(),
                                        List.of(new SacrificeSelfEffect()),
                                        true),
                                GrantScope.ENCHANTED_PERMANENT),
                        null));
    }
}
