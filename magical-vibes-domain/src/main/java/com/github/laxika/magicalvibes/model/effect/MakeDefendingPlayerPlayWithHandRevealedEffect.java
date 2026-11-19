package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may have defending player play with their hand revealed for as long as this creature remains
 * on the battlefield" (Stromgald Spy). Reads the defending player off the stack entry's
 * {@code targetId} and the source creature off its {@code sourcePermanentId}, and resolves into itself as a floating
 * effect on that player lasting {@link Duration#WHILE_SOURCE_REMAINS}.
 *
 * <p>Designed for {@code Triggers.attacksUnblocked()}. Wrap in a {@link MayEffect} for "you may", and pair
 * with {@link AssignNoCombatDamageEffect} inside a {@link SequenceEffect} for the "if you do, this
 * creature assigns no combat damage this turn" rider. Contrast the purely static
 * {@link PlayWithHandsRevealedEffect} / {@link PlayWithOwnHandRevealedEffect}.</p>
 */
public record MakeDefendingPlayerPlayWithHandRevealedEffect() implements CardEffect {
}
