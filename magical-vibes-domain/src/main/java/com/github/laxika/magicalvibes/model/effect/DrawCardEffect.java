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

import java.util.Objects;

/**
 * {@code player} draws {@code amount} cards, one at a time (so draw-replacement effects and "whenever you draw"
 * triggers see each individual draw). The player is {@link Subjects#you()} unless a constructor names another; several
 * players draw in APNAP order (CR 101.4), each the same evaluated number, or with {@link #eachEvaluatedFor} each their
 * own number.
 *
 * <p>Only {@link Subjects#targetPlayer()} declares a target (benign, any player). A controller draw whose amount reads
 * a target player ("cards equal to the number of cards in target opponent's hand") also declares one.
 *
 * @param amountPerPlayer the amount is evaluated once for each drawing player, player-relative scopes
 *                        ({@code CountScope.CONTROLLER}) reading that player, every player's number fixed before the
 *                        first card is drawn (CR 608.2h; {@link #eachEvaluatedFor}); otherwise once, for the effect's
 *                        controller
 */
public record DrawCardEffect(DynamicAmount amount, DynamicAmount castTimeXValue, boolean rememberAmount,
                             PlayerSubject player, boolean amountPerPlayer)
        implements CardDrawingEffect, CastTimeXValueEffect {

    public DrawCardEffect {
        Objects.requireNonNull(player, "player");
        if (amountPerPlayer && (rememberAmount || castTimeXValue != null || player.declaresTarget())) {
            throw new IllegalArgumentException("An amount per player is for players named without targeting, with no "
                    + "remembered or cast-time amount: " + player);
        }
    }

    public DrawCardEffect(DynamicAmount amount, DynamicAmount castTimeXValue, boolean rememberAmount,
                          PlayerSubject player) {
        this(amount, castTimeXValue, rememberAmount, player, false);
    }

    /**
     * "[Players] each draw [amount]" where the amount reads the drawing player ("each player draws a card for each
     * creature card in their graveyard", "each player draws cards equal to the difference"): every player's number is
     * worked out first, then they draw in APNAP order (CR 608.2h, 101.4).
     */
    public static DrawCardEffect eachEvaluatedFor(DynamicAmount amount, PlayerSubject players) {
        return new DrawCardEffect(amount, null, false, players, true);
    }

    public DrawCardEffect(DynamicAmount amount, DynamicAmount castTimeXValue, boolean rememberAmount) {
        this(amount, castTimeXValue, rememberAmount, Subjects.you());
    }

    public DrawCardEffect(DynamicAmount amount, DynamicAmount castTimeXValue) {
        this(amount, castTimeXValue, false);
    }

    /** {@code player} draws {@code amount} cards. */
    public DrawCardEffect(DynamicAmount amount, PlayerSubject player) {
        this(amount, null, false, player);
    }

    /** {@code player} draws {@code amount} cards. */
    public DrawCardEffect(int amount, PlayerSubject player) {
        this(new Fixed(amount), player);
    }

    /** Saves the evaluated count for subsequent effects before drawing can change the game state. */
    public static DrawCardEffect rememberingAmount(DynamicAmount amount) {
        return new DrawCardEffect(amount, null, true);
    }

    public DrawCardEffect(DynamicAmount amount) {
        this(amount, null, false);
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
        return new DrawCardEffect(amount, castTimeXValue);
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
    public PlayerSubject drawingPlayer() {
        return player;
    }

    @Override
    public TargetSpec targetSpec() {
        // A controller draw needs a player target only for a target-relative amount (e.g. Dream Salvage draws equal to
        // the number of cards target opponent discarded this turn; Recurring Insight draws equal to the number of cards
        // in target opponent's hand).
        boolean declares = player.declaresTarget() || player instanceof PlayerSubject.You && isTargetRelative();
        return declares ? TargetSpec.benign(TargetPredicates.player()) : TargetSpec.NONE;
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
