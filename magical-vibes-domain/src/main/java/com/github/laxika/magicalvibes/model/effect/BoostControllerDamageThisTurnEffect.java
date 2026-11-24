package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;

/**
 * One-shot effect: "If a [color] source you control would deal [noncombat] damage [to a permanent or player] this
 * turn, it deals that much damage plus N instead" (CR 614.1a). Resolving adds a floating effect on the controller
 * until end of turn carrying the bonus as a {@link Fixed} amount, fixed as it resolves (a spell's X: Taii Wakeen); it
 * changes the rules, so it also applies to sources that weren't there when it began (CR 611.2c). Each instance is its
 * own "plus N" option the affected player orders (CR 616.1).
 *
 * @param amount        N, evaluated as the effect resolves
 * @param color         the colour a source must have (The Flame of Keld), or {@code null} for any source
 * @param noncombatOnly whether only noncombat damage gets the bonus (Taii Wakeen)
 */
public record BoostControllerDamageThisTurnEffect(DynamicAmount amount, CardColor color, boolean noncombatOnly)
        implements CardEffect {

    public BoostControllerDamageThisTurnEffect {
        if (color != null && noncombatOnly) {
            throw new IllegalArgumentException("No coloured noncombat-only bonus is read");
        }
    }

    /** "If a source you control would deal damage this turn, it deals that much damage plus {@code amount}." */
    public BoostControllerDamageThisTurnEffect(int amount) {
        this(new Fixed(amount), null, false);
    }

    /** "If a {@code color} source you control would deal damage ... this turn, it deals that much damage plus N." */
    public static BoostControllerDamageThisTurnEffect fromSourcesOfColor(CardColor color, int amount) {
        return new BoostControllerDamageThisTurnEffect(new Fixed(amount), color, false);
    }

    /** "If a source you control would deal noncombat damage ... this turn, it deals that much damage plus X." */
    public static BoostControllerDamageThisTurnEffect noncombatPlusX() {
        return new BoostControllerDamageThisTurnEffect(new XValue(), null, true);
    }

    /** The bonus of a resolved, floating instance; {@code 0} for one that wasn't fixed. */
    public int fixedBonus() {
        return amount instanceof Fixed fixed ? fixed.value() : 0;
    }
}
