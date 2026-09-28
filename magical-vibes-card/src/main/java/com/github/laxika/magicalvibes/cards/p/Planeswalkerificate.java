package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentToughnessBecomesLoyaltyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCardTypeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.InitializeEnchantedPermanentToughnessAsLoyaltyEffect;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MB2", collectorNumber = "328")
@CardRegistration(set = "MB2", collectorNumber = "565")
public class Planeswalkerificate extends Card {

    public Planeswalkerificate() {
        target(TargetFilters.creatureYouControl());

        addEffect(EffectSlot.STATIC,
                new GrantCardTypeEffect(CardType.PLANESWALKER, GrantScope.ENCHANTED_PERMANENT));
        addEffect(EffectSlot.STATIC, new EnchantedPermanentToughnessBecomesLoyaltyEffect());
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new InitializeEnchantedPermanentToughnessAsLoyaltyEffect());

        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        +1,
                        List.of(new AwardManaEffect(ManaColor.RED, 2)),
                        "+1: Add {R}{R}."
                ).withToughnessAsLoyalty(),
                GrantScope.ENCHANTED_PERMANENT));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                new ActivatedAbility(
                        -1,
                        List.of(new ExileTopCardMayPlayThisTurnEffect(false)),
                        "−1: Exile the top card of your library. You may play it this turn."
                ).withToughnessAsLoyalty(),
                GrantScope.ENCHANTED_PERMANENT));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                ActivatedAbility.variableLoyaltyAbility(
                        List.of(new DealDamageToAnyTargetEffect(new XValue())),
                        "−X: This planeswalker deals X damage to any target.",
                        null
                ).withToughnessAsLoyalty(),
                GrantScope.ENCHANTED_PERMANENT));
    }
}
