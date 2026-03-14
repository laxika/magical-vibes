package com.github.laxika.magicalvibes.model;

/**
 * A mana payment component of a casting cost (e.g. "{G}" for Ancient Grudge's flashback).
 */
public record ManaCastingCost(String manaCost) implements CastingCost {
}
