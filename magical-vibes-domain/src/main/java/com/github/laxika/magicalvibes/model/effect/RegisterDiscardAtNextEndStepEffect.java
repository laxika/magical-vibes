package com.github.laxika.magicalvibes.model.effect;

/** Registers a delayed trigger that makes the controller discard cards at the next end step. */
public record RegisterDiscardAtNextEndStepEffect(int count) implements CardEffect {
}
