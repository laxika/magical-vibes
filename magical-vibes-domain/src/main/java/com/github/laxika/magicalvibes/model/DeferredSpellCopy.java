package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Copies of a spell just cast from a graveyard that are held back until the resolving spell has
 * finished casting every card it lets its controller cast ("copy each of those spells").
 */
public record DeferredSpellCopy(StackEntry original, Card cardToCast, UUID controllerId, int requestedCopies) {
}
