package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.ArrayList;
import java.util.List;

/** Exiles a targeted graveyard card, creates a copy of it, and offers the copy for casting. */
public record ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter,
        GraveyardSearchScope scope,
        int lifeLossOnCast,
        boolean targetPutIntoGraveyardFromAnywhereThisTurn,
        boolean withoutPayingManaCost,
        boolean matchManaValueToCombatDamage,
        CardEffect afterSuccessfulCastEffect
) implements CombatDamageAmountAwareEffect {
    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(CardPredicate filter, GraveyardSearchScope scope, boolean withoutPayingManaCost) {
        this(filter, scope, 0, false, withoutPayingManaCost, false, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter, GraveyardSearchScope scope) {
        this(filter, scope, 0, false, true, false, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
        CardPredicate filter, GraveyardSearchScope scope, int lifeLossOnCast) {
        this(filter, scope, lifeLossOnCast, false, true, false, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope, int lifeLossOnCast,
            boolean targetPutIntoGraveyardFromAnywhereThisTurn) {
        this(filter, scope, lifeLossOnCast, targetPutIntoGraveyardFromAnywhereThisTurn, true, false, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope, int lifeLossOnCast,
            boolean targetPutIntoGraveyardFromAnywhereThisTurn, boolean withoutPayingManaCost) {
        this(filter, scope, lifeLossOnCast, targetPutIntoGraveyardFromAnywhereThisTurn,
                withoutPayingManaCost, false, null);
    }

    public ExileTargetCardFromGraveyardAndMayCastCopyEffect(
            CardPredicate filter, GraveyardSearchScope scope, boolean matchManaValueToCombatDamage,
            CardEffect afterSuccessfulCastEffect) {
        this(filter, scope, 0, false, true, matchManaValueToCombatDamage, afterSuccessfulCastEffect);
    }

    @Override
    public DynamicAmount combatDamageAmount() {
        return matchManaValueToCombatDamage ? new EventValue() : new Fixed(0);
    }

    @Override
    public CardEffect snapshotCombatDamage(int damageDealt) {
        if (!matchManaValueToCombatDamage) {
            return this;
        }
        List<CardPredicate> predicates = new ArrayList<>();
        if (filter != null) {
            predicates.add(filter);
        }
        predicates.add(new CardMinManaValuePredicate(damageDealt));
        predicates.add(new CardMaxManaValuePredicate(damageDealt));
        return new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                new CardAllOfPredicate(predicates), scope, lifeLossOnCast,
                targetPutIntoGraveyardFromAnywhereThisTurn, withoutPayingManaCost, false,
                afterSuccessfulCastEffect);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }
}
