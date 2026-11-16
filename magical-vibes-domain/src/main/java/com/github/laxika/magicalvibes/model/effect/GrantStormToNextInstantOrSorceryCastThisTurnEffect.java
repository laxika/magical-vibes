package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

/**
 * "The next instant or sorcery spell you cast this turn has storm" (Storm, Force of Nature; Crackling Spellslinger):
 * each resolution gives that spell its own instance of storm ({@link #grantedAbility}), held as an ability that applies
 * only to the next matching spell this turn and ends with the turn if none is cast (CR 603.7b). Storm is the spell's own
 * triggered ability (CR 702.40a), so the spell is its source.
 */
public record GrantStormToNextInstantOrSorceryCastThisTurnEffect() implements CardEffect {

    /** The storm the next instant or sorcery spell gets: "when you cast this spell, copy it for each spell cast before it". */
    public TriggeredAbility grantedAbility() {
        return new TriggeredAbility(Triggers.youCastSpell()
                .withFilter(new CardAnyOfPredicate(List.of(
                        new CardTypePredicate(CardType.INSTANT), new CardTypePredicate(CardType.SORCERY))))
                .grantedToTheSpell(), CopyTriggeringSpellEffect.storm());
    }
}
