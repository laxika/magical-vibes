package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.ArrayList;
import java.util.List;

/**
 * Trigger-time descriptor for exiling a matching card from an opponent's graveyard whose mana
 * value is at most the triggering spell's mana value, then offering a free copy.
 */
public record ExileTargetCardFromOpponentGraveyardAndMayCastCopyAtTriggeringSpellManaValueEffect(
        CardPredicate filter,
        GraveyardSearchScope scope
) implements TriggeringSpellManaValueEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCards(filter, scope));
    }

    @Override
    public CardEffect snapshotTriggeringSpellManaValue(int manaValue) {
        List<CardPredicate> predicates = new ArrayList<>();
        if (filter != null) {
            predicates.add(filter);
        }
        predicates.add(new CardMaxManaValuePredicate(manaValue));
        return new ExileTargetCardFromGraveyardAndMayCastCopyEffect(
                new CardAllOfPredicate(predicates), scope);
    }
}
