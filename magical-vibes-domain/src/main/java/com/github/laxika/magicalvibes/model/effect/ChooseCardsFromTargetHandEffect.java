package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Reveal the target player's hand, the caster chooses {@code count} card(s), and each chosen card
 * goes to {@code destination} (discard / exile / top of library). The {@link DynamicAmount}
 * {@code count} covers fixed counts ("choose a card") and X values ("choose X cards", Mind Warp).
 * {@code includedTypes} restricts the choosable cards to those types (else {@code excludedTypes}
 * filters them out); an empty {@code includedTypes} means "any card not in excludedTypes".
 * {@code filter}, when non-null, narrows the choosable cards further by a {@link CardPredicate}
 * (e.g. "nonlegendary" for Lay Bare the Heart). {@code returnOnSourceLeave} applies only to
 * {@link HandChoiceDestination#EXILE} (return exiled cards when the source permanent leaves).
 */
public record ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                              HandChoiceDestination destination, boolean returnOnSourceLeave,
                                              CardPredicate filter) implements CombatDamageTriggerContextEffect {

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, null);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, CardPredicate filter,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false, filter);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, includedTypes, destination, false, null);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                           HandChoiceDestination destination, boolean returnOnSourceLeave) {
        this(new Fixed(count), excludedTypes, includedTypes, destination, returnOnSourceLeave, null);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes, HandChoiceDestination destination) {
        this(count, excludedTypes, List.of(), destination, false, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PLAYER);
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return TriggerContext.DAMAGED_PLAYER;
    }
}
