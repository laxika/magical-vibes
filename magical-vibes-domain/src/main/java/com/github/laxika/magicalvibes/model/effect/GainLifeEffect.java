package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;

/**
 * A player gains {@code amount} life. By default the spell/ability's controller gains it; a
 * {@code recipient} of {@link GainLifeRecipient#TARGET_CONTROLLER} instead routes the life to the
 * controller of the effect's target permanent (e.g. Condemn's "its controller gains life equal to
 * its toughness").
 *
 * <p>{@code targetsPlayer} makes the effect target a player (the controller still gains the life);
 * this only establishes the target so a {@code CountScope.TARGET_PLAYER} amount can read it — e.g.
 * Renewing Dawn's "gain 2 life for each Mountain target opponent controls".
 *
 * <p>{@code onlyIfSacrificed} marks an {@code ON_DEATH} effect as triggering only when its source
 * was sacrificed. Use {@link #sacrificeOnly(int)} or {@link #sacrificeOnly(DynamicAmount)} for that
 * form.
 */
public record GainLifeEffect(DynamicAmount amount, GainLifeRecipient recipient,
                             boolean targetsPlayer,
                             boolean onlyIfSacrificed) implements LifeGainEffect, CombatDamageTriggerContextEffect {

    public GainLifeEffect(DynamicAmount amount, GainLifeRecipient recipient) {
        this(amount, recipient, false, false);
    }

    public GainLifeEffect(DynamicAmount amount) {
        this(amount, GainLifeRecipient.CONTROLLER, false, false);
    }

    public GainLifeEffect(int amount) {
        this(new Fixed(amount));
    }

    public GainLifeEffect(DynamicAmount amount, GainLifeRecipient recipient, boolean targetsPlayer) {
        this(amount, recipient, targetsPlayer, false);
    }

    public static GainLifeEffect sacrificeOnly(int amount) {
        return sacrificeOnly(new Fixed(amount));
    }

    public static GainLifeEffect sacrificeOnly(DynamicAmount amount) {
        return new GainLifeEffect(amount, GainLifeRecipient.CONTROLLER, false, true);
    }

    @Override
    public DynamicAmount lifeGainAmount() {
        return amount;
    }

    @Override
    public TargetSpec targetSpec() {
        return targetsPlayer ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return amount instanceof PermanentCount count && count.scope() == CountScope.TARGET_PLAYER
                ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
