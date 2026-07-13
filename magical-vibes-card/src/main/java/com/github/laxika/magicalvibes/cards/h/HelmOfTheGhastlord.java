package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "SHM", collectorNumber = "166")
public class HelmOfTheGhastlord extends Card {

    public HelmOfTheGhastlord() {
        target(new PermanentPredicateTargetFilter(
                new PermanentIsCreaturePredicate(),
                "Target must be a creature"
        ))
                // As long as enchanted creature is blue, it gets +1/+1 and has
                // "Whenever this creature deals damage to an opponent, draw a card."
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE))))
                .addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLUE)),
                        new DrawCardEffect(),
                        null))
                // As long as enchanted creature is black, it gets +1/+1 and has
                // "Whenever this creature deals damage to an opponent, that player discards a card."
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        1, 1, GrantScope.ENCHANTED_CREATURE,
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK))))
                .addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new EnchantedPermanentConditionalEffect(
                        new PermanentColorInPredicate(Set.of(CardColor.BLACK)),
                        new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER, false),
                        null));
    }
}
