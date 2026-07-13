package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/**
 * Reveal the target player's hand, the caster chooses {@code count} card(s), and each chosen card
 * goes to {@code destination} (discard / exile / top of library). The {@link DynamicAmount}
 * {@code count} covers fixed counts ("choose a card") and X values ("choose X cards", Mind Warp).
 * {@code includedTypes} restricts the choosable cards to those types (else {@code excludedTypes}
 * filters them out); an empty {@code includedTypes} means "any card not in excludedTypes".
 * {@code returnOnSourceLeave} applies only to {@link HandChoiceDestination#EXILE} (return exiled
 * cards when the source permanent leaves).
 */
public record ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                              HandChoiceDestination destination, boolean returnOnSourceLeave) implements CardEffect {

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, List.of(), destination, false);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                           HandChoiceDestination destination) {
        this(new Fixed(count), excludedTypes, includedTypes, destination, false);
    }

    public ChooseCardsFromTargetHandEffect(int count, List<CardType> excludedTypes, List<CardType> includedTypes,
                                           HandChoiceDestination destination, boolean returnOnSourceLeave) {
        this(new Fixed(count), excludedTypes, includedTypes, destination, returnOnSourceLeave);
    }

    public ChooseCardsFromTargetHandEffect(DynamicAmount count, List<CardType> excludedTypes, HandChoiceDestination destination) {
        this(count, excludedTypes, List.of(), destination, false);
    }

    @Override
    public boolean canTargetPlayer() {
        return true;
    }
}
