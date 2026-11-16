package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.ResolvePendingExileReturnEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;

import java.util.List;
import java.util.UUID;

/**
 * Registers the delayed triggered ability that returns exiled cards when a step begins ("return it to the battlefield
 * at the beginning of the next end step", "at the beginning of your next upkeep"; CR 603.7a). The ability goes on the
 * stack as the step begins; the return, and the player's choices in it (what the returning permanent attacks, what an
 * Aura enchants), happen as it resolves.
 */
public final class PendingExileReturns {

    private PendingExileReturns() {
    }

    /** Registers the delayed ability of {@code pending}, which triggers the next time its step begins. */
    public static void schedule(GameData gameData, PendingExileReturn pending) {
        schedule(gameData, pending, DelayedReferents.cards(List.of(pending.card().getId())));
    }

    /**
     * Registers the delayed ability of {@code pending}, which triggers the next time its step begins after the turn
     * {@code turnNumber} ("at the beginning of the next turn's declare attackers step").
     */
    public static void scheduleAfterTurn(GameData gameData, PendingExileReturn pending, int turnNumber) {
        schedule(gameData, pending, DelayedReferents.cards(List.of(pending.card().getId())).afterTurn(turnNumber));
    }

    private static void schedule(GameData gameData, PendingExileReturn pending, DelayedReferents referents) {
        Card source = pending.triggerSourceCard() != null ? pending.triggerSourceCard() : pending.card();
        UUID controllerId = pending.triggerControllerId() != null
                ? pending.triggerControllerId() : pending.controllerId();
        ActivePlayer player = ActivePlayer.ANY;
        UUID timedPlayerId = null;
        if (pending.onlyOnControllersTurn()) {
            UUID timingId = pending.timingControllerId() != null
                    ? pending.timingControllerId() : pending.controllerId();
            if (timingId.equals(controllerId)) {
                player = ActivePlayer.YOU;
            } else {
                player = ActivePlayer.CHOSEN_PLAYER;
                timedPlayerId = timingId;
            }
        }
        long exileVersion = gameData.findExiledCard(pending.card().getId()) != null
                ? gameData.exileEntryVersions.getOrDefault(pending.card().getId(), -1L) : -1;
        StepTrigger step = new StepTrigger(moment(pending), player);
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(controllerId,
                source, new TriggeredAbility(step, new ResolvePendingExileReturnEffect(pending, exileVersion)), null,
                null, timedPlayerId, referents));
    }

    private static StepTrigger.Moment moment(PendingExileReturn pending) {
        return switch (pending.returnStep()) {
            case UPKEEP -> StepTrigger.Moment.UPKEEP;
            case BEGINNING_OF_COMBAT -> StepTrigger.Moment.BEGINNING_OF_COMBAT;
            case DECLARE_ATTACKERS -> StepTrigger.Moment.DECLARE_ATTACKERS;
            case END_STEP -> StepTrigger.Moment.END_STEP;
            default -> throw new IllegalStateException(pending.card().getName()
                    + " returns at a step no delayed ability waits for: " + pending.returnStep());
        };
    }
}
