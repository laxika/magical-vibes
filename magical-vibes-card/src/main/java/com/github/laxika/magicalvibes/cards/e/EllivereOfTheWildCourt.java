package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "2")
@CardRegistration(set = "WOC", collectorNumber = "36")
@CardRegistration(set = "WOC", collectorNumber = "57")
public class EllivereOfTheWildCourt extends Card {

    public EllivereOfTheWildCourt() {
        PermanentPredicate anotherCreatureYouControl = new PermanentAllOfPredicate(List.of(
                new PermanentIsCreaturePredicate(),
                new PermanentControlledBySourceControllerPredicate(),
                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
        ));
        target(new PermanentPredicateTargetFilter(
                anotherCreatureYouControl,
                "Target must be another creature you control"
        ))
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new CreateTokenAttachedToTargetEffect(virtuousRoleToken()))
                .addEffect(EffectSlot.ON_ATTACK,
                        new CreateTokenAttachedToTargetEffect(virtuousRoleToken()));
    }

    private static CreateTokenEffect virtuousRoleToken() {
        PermanentCount enchantmentsYouControl = new PermanentCount(
                new PermanentIsEnchantmentPredicate(), CountScope.CONTROLLER);
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Virtuous",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, SequenceEffect.of(
                        new DynamicStaticBoostEffect(
                                enchantmentsYouControl,
                                enchantmentsYouControl,
                                GrantScope.ENCHANTED_CREATURE),
                        new GrantTriggeredAbilityEffect(
                                EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                                new DrawCardEffect(1),
                                GrantScope.ENCHANTED_CREATURE))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
