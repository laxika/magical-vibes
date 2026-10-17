package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

/**
 * Matches permanents that currently carry the given static effect, printed or granted until end of turn
 * (for example the nonbasic landwalk of Dryad Sophisticate).
 */
public record PermanentHasStaticEffectPredicate(CardEffect effect) implements PermanentPredicate {
}
