package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Objects;

/** Applies a permanent animation to a returned permanent when its card was not a creature card. */
public record AnimateReturnedPermanentIfNotCreatureEffect(AnimatePermanentsEffect animation)
        implements CardEffect {

    public AnimateReturnedPermanentIfNotCreatureEffect {
        Objects.requireNonNull(animation);
        if (animation.scope() != GrantScope.TARGET || animation.duration() != EffectDuration.PERMANENT) {
            throw new IllegalArgumentException("Returned permanent animation must target permanently");
        }
    }

    public boolean appliesTo(Card card) {
        return !card.hasType(CardType.CREATURE);
    }
}
