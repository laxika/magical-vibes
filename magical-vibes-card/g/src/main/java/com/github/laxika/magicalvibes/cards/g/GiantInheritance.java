package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenAttachedToUpToOneTargetEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsSelfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "WOC", collectorNumber = "17")
@CardRegistration(set = "WOC", collectorNumber = "53")
public class GiantInheritance extends Card {

    public GiantInheritance() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(5, 5, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantTriggeredAbilityEffect(
                        EffectSlot.ON_ATTACK,
                        new CreateTokenAttachedToUpToOneTargetEffect(
                                monsterRoleToken(),
                                PlayerRelation.ANY,
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentIsAttackingPredicate()))),
                        GrantScope.ENCHANTED_CREATURE));
        // When this Aura is put into a graveyard from the battlefield, return it to its owner's hand.
        addEffect(EffectSlot.ON_SELF_PUT_INTO_GRAVEYARD_FROM_BATTLEFIELD,
                ReturnCardFromGraveyardEffect.builder()
                        .destination(GraveyardChoiceDestination.HAND)
                        .filter(new CardIsSelfPredicate())
                        .returnAll(true)
                        .build());
    }

    private static CreateTokenEffect monsterRoleToken() {
        return new CreateTokenEffect(
                CardType.ENCHANTMENT,
                1,
                "Monster",
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
                        StaticAbility.of(SequenceEffect.of(
                        new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE)))),
                List.of(),
                false,
                false,
                false,
                0,
                Set.<Keyword>of());
    }
}
