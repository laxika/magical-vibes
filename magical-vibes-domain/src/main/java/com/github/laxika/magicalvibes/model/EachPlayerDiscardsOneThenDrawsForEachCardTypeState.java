package com.github.laxika.magicalvibes.model;

import java.util.EnumSet;
import java.util.Set;

/** Card types among the cards discarded by an each-player discard, used to size the controller's draw. */
public class EachPlayerDiscardsOneThenDrawsForEachCardTypeState {

    public final Set<CardType> discardedCardTypes = EnumSet.noneOf(CardType.class);
}
