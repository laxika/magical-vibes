package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Sets one or more players' life totals to a computed amount. A single record covers the whole
 * "life total becomes" family: the {@link SetLifeTotalRecipient} routes whose total is set and the
 * {@link DynamicAmount} covers fixed values ("your life total becomes 5", Form of the Dragon) as
 * well as relational ones ({@code CardsInLibrary} for Invincible Hymn, {@code PermanentCount} for
 * Touch of the Eternal and Biorhythm). Negative amounts are clamped to 0.
 *
 * <p>Setting a life total is not damage and not plain life gain: per CR 119.5 the player gains or
 * loses the amount needed to reach the new total, so the change respects "life totals can't change"
 * / "can't gain life" effects and fires the corresponding gain/loss triggers.
 *
 * @param amount    the new life total
 * @param recipient whose life total is set
 */
public record SetLifeTotalEffect(DynamicAmount amount, SetLifeTotalRecipient recipient) implements CardEffect {

    /** Fixed new total for an explicit recipient. */
    public SetLifeTotalEffect(int amount, SetLifeTotalRecipient recipient) {
        this(new Fixed(amount), recipient);
    }

    /** Dynamic new total for the controller ("your life total becomes that number"). */
    public SetLifeTotalEffect(DynamicAmount amount) {
        this(amount, SetLifeTotalRecipient.CONTROLLER);
    }

    /** Fixed new total for the controller ("your life total becomes N"). */
    public SetLifeTotalEffect(int amount) {
        this(new Fixed(amount), SetLifeTotalRecipient.CONTROLLER);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == SetLifeTotalRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetPredicates.player())
                : TargetSpec.NONE;
    }
}
