package com.github.laxika.magicalvibes.model.filter;

/**
 * Matches permanents carrying a color chosen by the source permanent as it entered the battlefield.
 * Source-dependent: matches nothing when the source is gone or made no choice.
 */
public record PermanentHasSourceChosenColorPredicate() implements PermanentPredicate {
}
