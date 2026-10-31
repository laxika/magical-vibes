package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.SourceToughnessAtLeast;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToReturnedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOE", collectorNumber = "26")
public class ReturnTriumphant extends Card {

    public ReturnTriumphant() {
        var creatureCard = new CardAllOfPredicate(List.of(
                new CardTypePredicate(CardType.CREATURE),
                new CardMaxManaValuePredicate(3)));
        target(new GraveyardCardPredicateTargetFilter(
                creatureCard, GraveyardSearchScope.CONTROLLERS_GRAVEYARD))
                .addSpellEffect(ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.BATTLEFIELD)
                        .filter(creatureCard)
                        .targetGraveyard(true)
                        .build())
                .addSpellEffect(new CreateTokenAttachedToReturnedPermanentEffect(youngHeroRoleToken()));
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
                List.of(
                        StaticAbility.of(new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new ConditionalEffect(
                                new NotCondition(new SourceToughnessAtLeast(4)),
                                new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE)),
                        GrantScope.ENCHANTED_CREATURE))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
