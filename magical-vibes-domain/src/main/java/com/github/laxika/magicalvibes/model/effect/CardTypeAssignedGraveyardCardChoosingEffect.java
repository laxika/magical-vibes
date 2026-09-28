package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/** A graveyard choice whose selected cards must be assignable to distinct card-type slots. */
public interface CardTypeAssignedGraveyardCardChoosingEffect extends GraveyardCardChoosingEffect {

    Set<CardType> graveyardChoiceCardTypeSlots();
}
