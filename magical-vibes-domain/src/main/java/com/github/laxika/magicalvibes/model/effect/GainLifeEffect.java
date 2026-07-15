package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * A player gains {@code amount} life. By default the spell/ability's controller gains it; a
 * {@code recipient} of {@link GainLifeRecipient#TARGET_CONTROLLER} instead routes the life to the
 * controller of the effect's target permanent (e.g. Condemn's "its controller gains life equal to
 * its toughness").
 *
 * <p>{@code targetsPlayer} makes the effect target a player (the controller still gains the life);
 * this only establishes the target so a {@code CountScope.TARGET_PLAYER} amount can read it — e.g.
 * Renewing Dawn's "gain 2 life for each Mountain target opponent controls".
 */
public record GainLifeEffect(DynamicAmount amount, GainLifeRecipient recipient,
                             boolean targetsPlayer) implements LifeGainEffect {

    public GainLifeEffect(DynamicAmount amount, GainLifeRecipient recipient) {
        this(amount, recipient, false);
    }

    public GainLifeEffect(DynamicAmount amount) {
        this(amount, GainLifeRecipient.CONTROLLER, false);
    }

    public GainLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }
}
