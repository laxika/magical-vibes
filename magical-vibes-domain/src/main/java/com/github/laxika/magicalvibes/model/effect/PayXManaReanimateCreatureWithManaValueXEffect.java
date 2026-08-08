package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;

/**
 * On resolution, the controller may pay {X}: they choose X (up to their available mana) and that
 * mana is paid. When they do, a reflexive triggered ability goes on the stack targeting a creature
 * card with mana value X in the controller's graveyard; on its resolution that card is returned to
 * the battlefield under the controller's control.
 *
 * <p>Models "you may pay {X}. When you do, return target creature card with mana value X from your
 * graveyard to the battlefield ..." (Isareth the Awakener). Choosing X=0 means the controller
 * declines. The target of the reflexive trigger is chosen as it goes on the stack, after X is
 * locked in, so opponents can respond to a trigger with a known target.</p>
 *
 * @param enterWithCounter          when non-null, one counter of that type is put on the returned
 *                                  permanent as it enters (Isareth's corpse counter)
 * @param exileIfLeavesBattlefield  {@code true} to give the returned permanent the "if it would
 *                                  leave the battlefield, exile it instead" replacement
 */
public record PayXManaReanimateCreatureWithManaValueXEffect(
        CounterType enterWithCounter,
        boolean exileIfLeavesBattlefield
) implements CardEffect {
}
