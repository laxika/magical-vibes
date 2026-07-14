package com.github.laxika.magicalvibes.model.effect;

/**
 * Marker interface for effects whose primary action is countering a spell on the stack. Lets
 * consumers — chiefly the AI instant classifier — recognise a counterspell without listing every
 * concrete counter variant, mirroring how {@link ManaProducingEffect} marks mana production.
 *
 * <p>Descriptive only: membership is a fact about the effect, never a score.
 */
public interface CounterSpellingEffect extends CardEffect {
}
