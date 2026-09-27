package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

import java.util.Set;

/** Exiles up to one card of each card type from an opponent's graveyard. */
public record ExileUpToOneOfEachCardTypeFromGraveyardEffect()
        implements CardTypeAssignedGraveyardCardChoosingEffect {

    private static final Set<CardType> CARD_TYPE_SLOTS = Set.of(
            CardType.ARTIFACT,
            CardType.BATTLE,
            CardType.CREATURE,
            CardType.ENCHANTMENT,
            CardType.INSTANT,
            CardType.KINDRED,
            CardType.LAND,
            CardType.PLANESWALKER,
            CardType.SORCERY);

    @Override
    public int graveyardChoiceMaxTargets() {
        return CARD_TYPE_SLOTS.size();
    }

    @Override
    public Set<CardType> graveyardChoiceCardTypeSlots() {
        return CARD_TYPE_SLOTS;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.graveyardCard(GraveyardSearchScope.OPPONENT_GRAVEYARD));
    }
}
