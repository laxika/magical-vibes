package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Sets one or more players' life totals to a computed amount. A single record covers the whole
 * "life total becomes" family: the {@link PlayerSubject} names whose total is set and the
 * {@link DynamicAmount} covers fixed values ("your life total becomes 5", Form of the Dragon) as
 * well as relational ones ({@code CardsInLibrary} for Invincible Hymn, {@code PermanentCount} for
 * Touch of the Eternal and Biorhythm). Negative amounts are clamped to 0.
 *
 * <p>For an {@code each(...)} subject ("each player's life total becomes …"; Worldfire, Biorhythm, Arbiter of
 * Knollridge) the amount is evaluated once <em>per player</em>, with that player standing in as the amount's
 * controller, so a {@code CountScope.CONTROLLER} amount reads "the number of creatures <em>they</em> control"; every
 * new total is determined before any of them is applied. Any other subject evaluates the amount once, from the
 * controller's point of view.
 *
 * <p>Setting a life total is not damage and not plain life gain: per CR 119.5 the player gains or
 * loses the amount needed to reach the new total, so the change respects "life totals can't change"
 * / "can't gain life" effects and fires the corresponding gain/loss triggers.
 *
 * @param amount the new life total
 * @param player whose life total is set; only {@code targetPlayer()} declares a (benign) player target
 */
public record SetLifeTotalEffect(DynamicAmount amount, PlayerSubject player) implements CardEffect {

    /** Fixed new total for the named players. */
    public SetLifeTotalEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player);
    }

    /** Dynamic new total for the controller ("your life total becomes that number"). */
    public SetLifeTotalEffect(DynamicAmount amount) {
        this(amount, Subjects.you());
    }

    /** Fixed new total for the controller ("your life total becomes N"). */
    public SetLifeTotalEffect(int amount) {
        this(new Fixed(amount), Subjects.you());
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget()
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
