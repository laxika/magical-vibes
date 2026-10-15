package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtMostPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Map;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "140")
public class MerryBards extends Card {

    public MerryBards() {
        setCastTimeTargetFilter(TargetFilters.creatureYouControl());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                MayPayManaEffect.reflexiveTarget("{1}",
                        new CreateTokenAttachedToTargetEffect(youngHeroRoleToken()),
                        "Pay {1} to create a Young Hero Role token?"));
    }

    private static CreateTokenEffect youngHeroRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Young Hero",
                0,
                0,
                null,
                null,
                List.of(CardSubtype.AURA, CardSubtype.ROLE),
                Set.of(),
                Set.of(),
                false,
                false,
                Map.of(EffectSlot.STATIC, new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new TriggeringPermanentConditionalEffect(
                                new PermanentToughnessAtMostPredicate(3),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        GrantScope.ENCHANTED_CREATURE)),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
