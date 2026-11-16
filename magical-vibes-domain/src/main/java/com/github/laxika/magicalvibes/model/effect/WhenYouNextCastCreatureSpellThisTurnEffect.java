package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Creates the delayed triggered ability "When you next cast a creature spell this turn, {@code effect}."
 * ({@link #delayedAbility}), which triggers only the next time it applies and ends with the turn if it hasn't
 * (CR 603.7b). Its entry names that spell as its triggering card and spell.
 *
 * @param effect the effect the delayed ability resolves
 */
public record WhenYouNextCastCreatureSpellThisTurnEffect(CardEffect effect) implements CardEffect {

    /** The delayed triggered ability this creates. */
    public TriggeredAbility delayedAbility() {
        return new TriggeredAbility(Triggers.youCastSpell().withFilter(new CardTypePredicate(CardType.CREATURE)),
                effect);
    }
}
