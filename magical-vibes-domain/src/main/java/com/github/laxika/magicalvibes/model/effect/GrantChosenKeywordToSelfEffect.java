package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;

/**
 * On resolution, prompts the controller to choose one keyword from the given options,
 * then grants that keyword to the source permanent (this creature) until end of turn.
 * Non-targeted self grant — contrast {@link GrantChosenKeywordToTargetEffect}.
 * Used by Urza's Avenger's activated ability.
 */
public record GrantChosenKeywordToSelfEffect(List<Keyword> options) implements CardEffect {
}
