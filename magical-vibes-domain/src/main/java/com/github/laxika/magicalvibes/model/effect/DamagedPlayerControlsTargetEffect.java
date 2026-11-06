package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * A reflexive "when you do" ability whose permanent target is one "that player controls", the player a combat damage
 * trigger was about: Dokuchi Silencer's "you may discard a creature card. When you do, destroy target creature or
 * planeswalker that player controls". {@code DiscardCardThenEffectHandler} binds the player before the reflexive
 * target is chosen. Every other "that player controls" target reads the stack entry's triggering player through
 * {@code PermanentControlledByTriggeringPlayerPredicate}; this binding is left until the discard follow-up carries the
 * triggering player too.
 */
public interface DamagedPlayerControlsTargetEffect extends CardEffect {

    PermanentPredicate predicate();

    CardEffect forDamagedPlayer(UUID playerId);
}
