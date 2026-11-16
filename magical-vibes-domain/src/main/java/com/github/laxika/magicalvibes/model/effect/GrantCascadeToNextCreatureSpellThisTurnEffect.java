package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * "The next creature spell you cast this turn has cascade" (Sloppity Bilepiper): each resolution gives that spell its
 * own instance of cascade ({@link #grantedAbility}), held as an ability that applies only to the next creature spell this
 * turn and ends with the turn if none is cast (CR 603.7b). Cascade is the spell's own triggered ability (CR 702.85a), so
 * the spell is its source.
 */
public record GrantCascadeToNextCreatureSpellThisTurnEffect() implements CardEffect {

    /** The cascade the next creature spell gets. */
    public TriggeredAbility grantedAbility() {
        return new TriggeredAbility(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE))
                .grantedToTheSpell(), new CascadeEffect());
    }
}
