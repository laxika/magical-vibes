package com.github.laxika.magicalvibes.model.effect;

/**
 * "You may have defending player play with their hand revealed for as long as this creature remains
 * on the battlefield" (Stromgald Spy). Reads the defending player off the stack entry's
 * {@code targetId} and the source creature off its {@code sourcePermanentId}, then records the pair
 * in {@code GameData.handsRevealedWhileSourceOnBattlefield}; the reveal lasts only while that source
 * permanent is still on the battlefield.
 *
 * <p>Designed for {@code ON_ATTACKS_UNBLOCKED}. Wrap in a {@link MayEffect} for "you may", and pair
 * with {@link AssignNoCombatDamageEffect} inside a {@link SequenceEffect} for the "if you do, this
 * creature assigns no combat damage this turn" rider. Contrast the purely static
 * {@link PlayWithHandsRevealedEffect} / {@link PlayWithOwnHandRevealedEffect}.</p>
 */
public record MakeDefendingPlayerPlayWithHandRevealedEffect() implements CardEffect {
}
