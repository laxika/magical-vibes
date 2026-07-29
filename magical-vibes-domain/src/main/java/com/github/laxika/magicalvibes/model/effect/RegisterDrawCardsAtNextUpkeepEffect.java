package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * When resolved, registers a delayed trigger so a player draws {@code count} cards at the beginning
 * of the next turn's upkeep. Used for "Draw a card at the beginning of the next turn's upkeep"
 * (e.g. Blessed Wine) and, with {@code targetPlayer=true}, for "Target player draws a card at the
 * beginning of the next turn's upkeep" (e.g. Sapphire Charm). Drained in
 * {@code StepTriggerService.handleUpkeepTriggers}.
 *
 * @param count        the number of cards drawn at the next upkeep
 * @param targetPlayer whether the draw is registered for the targeted player instead of the
 *                     resolving controller
 */
public record RegisterDrawCardsAtNextUpkeepEffect(int count, boolean targetPlayer) implements CardDrawingEffect {

    public RegisterDrawCardsAtNextUpkeepEffect() {
        this(1);
    }

    public RegisterDrawCardsAtNextUpkeepEffect(int count) {
        this(count, false);
    }

    /**
     * "Target player draws {@code count} cards at the beginning of the next turn's upkeep."
     */
    public static RegisterDrawCardsAtNextUpkeepEffect targetPlayer(int count) {
        return new RegisterDrawCardsAtNextUpkeepEffect(count, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetPlayer ? TargetSpec.benign(TargetCategory.PLAYER) : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(count);
    }
}
