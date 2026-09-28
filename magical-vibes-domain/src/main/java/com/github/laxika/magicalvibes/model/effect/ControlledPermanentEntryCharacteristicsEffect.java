package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;
import java.util.Set;

/**
 * Capability for a static effect that permanently stamps characteristics onto a matching
 * permanent as it enters the battlefield.
 */
public interface ControlledPermanentEntryCharacteristicsEffect extends CardEffect {

    PermanentPredicate enteringPermanentPredicate();

    Integer basePower();

    Integer baseToughness();

    List<CardSubtype> addedSubtypes();

    default Set<CardType> addedCardTypes() {
        return Set.of(CardType.CREATURE);
    }
}
