package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.Objects;

/**
 * "The next [matching] spell you cast this turn has [ability]" for a triggered ability of the spell's own (storm,
 * CR 702.40a; cascade, CR 702.85a): each resolution gives that spell its own instance ({@link #grantedAbility}),
 * held as an ability that applies only to the next matching spell this turn and ends with the turn if none is cast
 * (CR 603.7b). The spell is its source.
 *
 * @param filter  the spells the next of which has the ability
 * @param effect  what the ability does when the spell is cast
 * @param keyword the ability's name, for the log
 */
public record GrantAbilityToNextSpellThisTurnEffect(CardPredicate filter, CardEffect effect, String keyword)
        implements CardEffect {

    public GrantAbilityToNextSpellThisTurnEffect {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(effect, "effect");
    }

    /** "The next [matching] spell you cast this turn has storm." */
    public static GrantAbilityToNextSpellThisTurnEffect storm(CardPredicate filter) {
        return new GrantAbilityToNextSpellThisTurnEffect(filter, CopyTriggeringSpellEffect.storm(), "storm");
    }

    /** "The next [matching] spell you cast this turn has cascade." */
    public static GrantAbilityToNextSpellThisTurnEffect cascade(CardPredicate filter) {
        return new GrantAbilityToNextSpellThisTurnEffect(filter, new CascadeEffect(), "cascade");
    }

    /** The ability the next matching spell gets. */
    public TriggeredAbility grantedAbility() {
        return new TriggeredAbility(Triggers.youCastSpell().withFilter(filter).grantedToTheSpell(), effect);
    }
}
