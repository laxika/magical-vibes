package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * When resolved, registers a delayed trigger so a player draws {@code count} cards at the beginning
 * of the next turn's upkeep. Used for "Draw a card at the beginning of the next turn's upkeep"
 * (e.g. Blessed Wine), for "Target player draws a card at the beginning of the next turn's upkeep"
 * (e.g. Sapphire Charm) and, with {@link UpkeepDrawRecipient#TARGET_SPELL_CONTROLLER} plus
 * {@code upTo}, for "Its controller may draw up to two cards at the beginning of the next turn's
 * upkeep" (Arcane Denial). Drained in {@code StepTriggerService.handleUpkeepTriggers}.
 *
 * @param count     the maximum number of cards drawn at the next upkeep
 * @param recipient who the draw is registered for
 * @param upTo      whether the drawer chooses any number from 0 to {@code count} at the upkeep
 *                  instead of drawing {@code count} automatically
 */
public record RegisterDrawCardsAtNextUpkeepEffect(int count, UpkeepDrawRecipient recipient, boolean upTo)
        implements CardDrawingEffect {

    public RegisterDrawCardsAtNextUpkeepEffect() {
        this(1);
    }

    public RegisterDrawCardsAtNextUpkeepEffect(int count) {
        this(count, UpkeepDrawRecipient.CONTROLLER, false);
    }

    /**
     * "Target player draws {@code count} cards at the beginning of the next turn's upkeep."
     */
    public static RegisterDrawCardsAtNextUpkeepEffect targetPlayer(int count) {
        return new RegisterDrawCardsAtNextUpkeepEffect(count, UpkeepDrawRecipient.TARGET_PLAYER, false);
    }

    /**
     * "[The targeted spell's] controller may draw up to {@code count} cards at the beginning of the
     * next turn's upkeep."
     */
    public static RegisterDrawCardsAtNextUpkeepEffect targetSpellControllerMayDrawUpTo(int count) {
        return new RegisterDrawCardsAtNextUpkeepEffect(count, UpkeepDrawRecipient.TARGET_SPELL_CONTROLLER, true);
    }

    /**
     * "That player draws {@code count} cards at the beginning of the next turn's upkeep", where the
     * player is the owner of the graveyard this entry's targeted cards are in (Lodestone Bauble).
     * Must be listed before any effect that moves those cards out of the graveyard.
     */
    public static RegisterDrawCardsAtNextUpkeepEffect targetGraveyardOwner(int count) {
        return new RegisterDrawCardsAtNextUpkeepEffect(count, UpkeepDrawRecipient.TARGET_GRAVEYARD_OWNER, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return recipient == UpkeepDrawRecipient.TARGET_PLAYER
                ? TargetSpec.benign(TargetCategory.PLAYER)
                : TargetSpec.NONE;
    }

    @Override
    public DynamicAmount drawnCardAmount() {
        return new Fixed(count);
    }
}
