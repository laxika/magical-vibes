package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CastingOption;

import java.util.Optional;
import java.util.Set;

/**
 * Capability for an emblem marker that grants a casting option to matching cards in its
 * controller's graveyard.
 */
public interface EmblemGrantsCastingOptionEffect extends CardEffect {

    Set<CardType> cardTypes();

    Optional<? extends CastingOption> castingOption(Card card);
}
