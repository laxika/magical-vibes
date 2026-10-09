package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.CardsDiscardedByTargetPlayerThisTurn;
import com.github.laxika.magicalvibes.model.amount.CardsInGraveyard;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.DistinctCountersOnSource;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.amount.SourceToughness;

/**
 * Controller draws {@code amount} cards, one at a time (so draw-replacement effects and
 * "whenever you draw" triggers see each individual draw).
 */
public record DrawCardEffect(DynamicAmount amount, boolean onlyIfSacrificed,
                             DynamicAmount castTimeXValue, boolean rememberAmount)
        implements ManaAbilityCardDrawingEffect, CombatDamageTriggerContextEffect,
        CastTimeXValueEffect {

    public DrawCardEffect(DynamicAmount amount, boolean onlyIfSacrificed, DynamicAmount castTimeXValue) {
        this(amount, onlyIfSacrificed, castTimeXValue, false);
    }

    /** Saves the evaluated count for subsequent effects before drawing can change the game state. */
    public static DrawCardEffect rememberingAmount(DynamicAmount amount) {
        return new DrawCardEffect(amount, false, null, true);
    }

    public DrawCardEffect(DynamicAmount amount, boolean onlyIfSacrificed) {
        this(amount, onlyIfSacrificed, null);
    }

    public DrawCardEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public DrawCardEffect() {
        this(1);
    }

    public DrawCardEffect(int amount) {
        this(new Fixed(amount));
    }

    /**
     * Draws using the spell's cast-time X value, which is calculated from {@code castTimeXValue}
     * before the spell is put on the stack.
     */
    public static DrawCardEffect withCastTimeXValue(DynamicAmount castTimeXValue,
                                                      DynamicAmount amount) {
        return new DrawCardEffect(amount, false, castTimeXValue);
    }

    public static DrawCardEffect sacrificeOnly(int amount) {
        return new DrawCardEffect(new Fixed(amount), true);
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return amount;
    }

    @Override
    public DynamicAmount castTimeXValue() {
        return castTimeXValue;
    }

    @Override
    public boolean onlyTriggersOnSacrifice() {
        return onlyIfSacrificed;
    }

    @Override
    public TriggerContext combatDamageTriggerContext() {
        return amount instanceof SourcePower || amount instanceof SourceToughness
                || amount instanceof DistinctCountersOnSource
                ? TriggerContext.SOURCE_SELF : null;
    }

    @Override
    public TargetSpec targetSpec() {
        // Only target-relative amounts require a player target on the stack entry (e.g. Dream
        // Salvage draws equal to the number of cards target opponent discarded this turn; Recurring
        // Insight draws equal to the number of cards in target opponent's hand).
        return isTargetRelative() ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
    }

    private boolean isTargetRelative() {
        if (amount instanceof CardsDiscardedByTargetPlayerThisTurn) {
            return true;
        }
        if (amount instanceof CardsInHand count && count.scope() == CountScope.TARGET_PLAYER) {
            return true;
        }
        if (amount instanceof CardsInGraveyard count && count.scope() == CountScope.TARGET_PLAYER) {
            return true;
        }
        return amount instanceof PermanentCount count && count.scope() == CountScope.TARGET_PLAYER;
    }
}
