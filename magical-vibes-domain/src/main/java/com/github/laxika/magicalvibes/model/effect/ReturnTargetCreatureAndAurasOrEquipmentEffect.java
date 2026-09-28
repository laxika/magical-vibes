package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

/** Returns a targeted creature, then up to two targeted Auras or Equipment attached to it. */
public record ReturnTargetCreatureAndAurasOrEquipmentEffect() implements CardEffect, TargetCardGroupEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(
                new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.CREATURE),
                        new CardSubtypePredicate(CardSubtype.AURA),
                        new CardSubtypePredicate(CardSubtype.EQUIPMENT))),
                GraveyardSearchScope.CONTROLLERS_GRAVEYARD));
    }

    @Override
    public List<Integer> targetGroups() {
        return List.of(0, 1);
    }
}
