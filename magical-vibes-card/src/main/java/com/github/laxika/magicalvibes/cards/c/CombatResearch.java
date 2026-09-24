package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSupertypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DMU", collectorNumber = "44")
public class CombatResearch extends Card {

    public CombatResearch() {
        target(TargetFilters.creature())
                // Enchanted creature has "Whenever this creature deals combat damage to a player,
                // draw a card."
                .addEffect(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                        new DrawCardEffect(1),
                        GrantScope.ENCHANTED_CREATURE))
                // As long as enchanted creature is legendary, it gets +1/+1.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, GrantScope.ENCHANTED_CREATURE,
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY)))
                // As long as enchanted creature is legendary, it has ward {1}.
                .addEffect(EffectSlot.STATIC, new EnchantedPermanentConditionalEffect(
                        new PermanentHasSupertypePredicate(CardSupertype.LEGENDARY),
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL,
                                new CounterUnlessPaysEffect(1),
                                GrantScope.ENCHANTED_CREATURE),
                        null));
    }
}
