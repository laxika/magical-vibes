package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * The controller chooses an opponent, then that opponent chooses a matching permanent the
 * controller controls to sacrifice.
 */
public record OpponentChoosesPermanentToSacrificeEffect(PermanentPredicate filter) implements CardEffect {
}
