package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventDamageEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;

import java.util.List;

@CardRegistration(set = "PLC", collectorNumber = "154")
public class CauterySliver extends Card {

    public CauterySliver() {
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                damageAbility(), GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                damageAbility(), GrantScope.SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                preventDamageAbility(), GrantScope.ALL_CREATURES,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
        addEffect(EffectSlot.STATIC, new GrantActivatedAbilityEffect(
                preventDamageAbility(), GrantScope.SELF,
                new PermanentHasSubtypePredicate(CardSubtype.SLIVER)));
    }

    private static ActivatedAbility damageAbility() {
        return new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), new DealDamageToAnyTargetEffect(1)),
                "{1}, Sacrifice this permanent: This permanent deals 1 damage to any target.");
    }

    private static ActivatedAbility preventDamageAbility() {
        return new ActivatedAbility(
                false,
                "{1}",
                List.of(new SacrificeSelfCost(), PreventDamageEffect.nextToTarget(1)),
                "{1}, Sacrifice this permanent: Prevent the next 1 damage that would be dealt to target player, planeswalker, or Sliver creature this turn.",
                cauteryTargetFilter());
    }

    private static AnyTargetPredicateTargetFilter cauteryTargetFilter() {
        return new AnyTargetPredicateTargetFilter(
                new PermanentAnyOfPredicate(List.of(
                        new PermanentIsPlaneswalkerPredicate(),
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.SLIVER))))),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player, planeswalker, or Sliver creature");
    }
}
