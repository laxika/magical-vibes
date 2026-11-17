package com.github.laxika.magicalvibes.model.effect;

import java.util.List;
import java.util.UUID;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * The player the enclosing ability targets gets a delayed triggered ability (CR 603.7a) that triggers when they cast a
 * spell matching {@code spellFilter} (any spell when {@code null}), for {@code duration}: "target player ... The next
 * spell they cast this turn has cascade" (Bigger on the Inside), "target opponent gets a one-time boon with 'When you
 * cast a creature spell, ...'" (Loch Larent). The targeted counterpart of
 * {@link RegisterDelayedControllerSpellCastTriggerEffect}.
 */
public record RegisterDelayedTargetPlayerSpellCastTriggerEffect(CardPredicate spellFilter,
                                                                List<CardEffect> resolvedEffects,
                                                                Duration duration,
                                                                boolean once) implements CardEffect {

    /** The target player gets a one-time boon with "When you cast a [spellFilter] spell, [effects]." */
    public static RegisterDelayedTargetPlayerSpellCastTriggerEffect boon(CardPredicate spellFilter,
                                                                        List<CardEffect> resolvedEffects) {
        return new RegisterDelayedTargetPlayerSpellCastTriggerEffect(spellFilter, resolvedEffects, Duration.PERMANENT,
                true);
    }

    /** "The next [spellFilter] spell [the target player] casts this turn [effects]." */
    public static RegisterDelayedTargetPlayerSpellCastTriggerEffect nextThisTurn(CardPredicate spellFilter,
                                                                                List<CardEffect> resolvedEffects) {
        return new RegisterDelayedTargetPlayerSpellCastTriggerEffect(spellFilter, resolvedEffects,
                Duration.UNTIL_END_OF_TURN, true);
    }

    /**
     * The delayed triggered ability this makes of the target player {@code playerId}, from a resolution of an object
     * with the source {@code sourceCard} and {@code sourcePermanentId} (or none) on turn {@code turnNumber}.
     */
    public TemporaryGlobalTriggeredAbility delayed(UUID playerId, Card sourceCard, UUID sourcePermanentId,
                                                   int turnNumber) {
        return RegisterDelayedControllerSpellCastTriggerEffect.delayed(duration, once, playerId, sourceCard,
                delayedAbility(), sourcePermanentId, turnNumber, null);
    }

    /** The delayed triggered ability the target player gets: "whenever you cast" a matching spell, its effects. */
    public TriggeredAbility delayedAbility() {
        return RegisterDelayedControllerSpellCastTriggerEffect.castAbility(spellFilter, null, resolvedEffects);
    }
}
