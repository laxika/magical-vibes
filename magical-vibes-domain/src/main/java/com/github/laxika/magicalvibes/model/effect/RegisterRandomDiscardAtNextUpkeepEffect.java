package com.github.laxika.magicalvibes.model.effect;

/** Registers a delayed trigger that makes the controller discard cards at random at their next upkeep. */
public record RegisterRandomDiscardAtNextUpkeepEffect(int count) implements CardEffect {
}
