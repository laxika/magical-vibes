package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** For each opponent, the controller chooses one matching permanent that opponent controls to destroy. */
public record EachOpponentChoosesPermanentToDestroyEffect(PermanentPredicate filter) implements CardEffect {
}
