package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player chooses a matching permanent in APNAP order. All chosen permanents are sacrificed
 * simultaneously; players who could not choose one then choose a card to discard, and those cards
 * are discarded simultaneously.
 *
 * @param filter the permanents each player may sacrifice
 */
public record EachPlayerSacrificesPermanentOrDiscardsEffect(PermanentPredicate filter)
        implements CardEffect {
}
