package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TormentState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link TormentOfHailfireEffect}: "Repeat the following process X times. Each opponent
 * loses N life unless that player sacrifices a nonland permanent of their choice or discards a card."
 *
 * <p>The flow is driven one opponent at a time and re-runs on every choice completion (kept alive via
 * {@link GameData#rerunCurrentEffectAfterInteraction}), mirroring
 * {@link EachPlayerMayDiscardUpToThenTakeDamageEffectHandler}. Progress lives on {@link GameData#torment}:
 * {@code remainingIterations} counts whole passes still to do; {@code remaining} is the APNAP opponent
 * queue for the current pass. For each opponent the offered options are pruned to what they can do (life
 * is always offered); an opponent whose only option is to lose life takes it immediately without a prompt.
 * Otherwise a three-way {@link PendingInteraction.ColorChoice} list-pick is begun; the chosen mode then
 * runs a sacrifice ({@link PermanentChoiceContext.TormentSacrifice}) or discard sub-choice, or applies
 * the life loss inline. Each sub-choice completion re-runs this handler to advance to the next opponent.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TormentOfHailfireEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final LifeSupport lifeSupport;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TormentOfHailfireEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int lifeLoss = ((TormentOfHailfireEffect) effect).lifeLoss();
        TormentState state = gameData.torment;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            // Fresh entry: seed the iteration counter (X) and start processing.
            state.reset();
            state.active = true;
            state.remainingIterations = Math.max(0, entry.getXValue());
            advance(gameData, entry, sourceName, lifeLoss);
            return;
        }

        if (state.chosenMode != null) {
            // The current opponent just picked a penalty option — apply it.
            String mode = state.chosenMode;
            state.chosenMode = null;
            applyMode(gameData, entry, sourceName, lifeLoss, state.currentOpponentId, mode);
            return;
        }

        // Re-entry after a discard / sacrifice sub-choice completed — advance to the next opponent.
        advance(gameData, entry, sourceName, lifeLoss);
    }

    /**
     * Processes opponents until a penalty choice must be prompted (then returns, pausing resolution)
     * or the whole X-times process finishes. Opponents whose only option is to lose life take it
     * inline without a prompt; each new iteration refills the APNAP opponent queue.
     */
    private void advance(GameData gameData, StackEntry entry, String sourceName, int lifeLoss) {
        TormentState state = gameData.torment;
        UUID controllerId = entry.getControllerId();
        while (true) {
            if (state.remaining.isEmpty()) {
                if (state.remainingIterations <= 0) {
                    gameData.rerunCurrentEffectAfterInteraction = false;
                    state.reset();
                    return;
                }
                state.remainingIterations--;
                state.remaining.addAll(apnapOpponents(gameData, controllerId));
                if (state.remaining.isEmpty()) {
                    // No opponents to process this iteration.
                    continue;
                }
            }

            UUID opponentId = state.remaining.pollFirst();
            if (!gameData.playerIds.contains(opponentId)) {
                continue;
            }
            state.currentOpponentId = opponentId;

            List<String> options = availableOptions(gameData, opponentId, lifeLoss);
            if (options.size() == 1) {
                // Only "lose life" is possible — no choice to make.
                lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
                continue;
            }

            gameData.rerunCurrentEffectAfterInteraction = true;
            String prompt = sourceName + " — lose " + lifeLoss
                    + " life unless you sacrifice a nonland permanent or discard a card.";
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    opponentId, null, null,
                    new ChoiceContext.TormentPenaltyChoice(opponentId, sourceName),
                    options, prompt));
            return;
        }
    }

    /**
     * Applies the current opponent's chosen penalty. Sacrifice/discard begin a sub-choice and pause
     * (advancing on completion via the re-run); losing life applies immediately and continues.
     */
    private void applyMode(GameData gameData, StackEntry entry, String sourceName, int lifeLoss,
            UUID opponentId, String mode) {
        if (ChoiceContext.TormentPenaltyChoice.SACRIFICE.equals(mode)) {
            List<UUID> nonlandIds = nonlandPermanentIds(gameData, opponentId);
            if (nonlandIds.isEmpty()) {
                advance(gameData, entry, sourceName, lifeLoss);
                return;
            }
            gameData.rerunCurrentEffectAfterInteraction = true;
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.TormentSacrifice(opponentId));
            playerInputService.beginPermanentChoice(gameData, opponentId, nonlandIds,
                    sourceName + " — choose a nonland permanent to sacrifice.");
            return;
        }

        if (ChoiceContext.TormentPenaltyChoice.DISCARD.equals(mode)) {
            List<Card> hand = gameData.playerHands.get(opponentId);
            if (hand == null || hand.isEmpty()) {
                advance(gameData, entry, sourceName, lifeLoss);
                return;
            }
            gameData.discardCausedByOpponent = !opponentId.equals(entry.getControllerId());
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInteractionSupport.resolveDiscardCards(gameData, opponentId, 1, DiscardFollowUp.NONE);
            if (!gameData.interaction.isAwaitingInput()) {
                // Defensive: the hand emptied out from under us — just continue.
                advance(gameData, entry, sourceName, lifeLoss);
            }
            return;
        }

        // "Lose N life": the player declined to sacrifice or discard.
        lifeSupport.applyLifeLoss(gameData, opponentId, lifeLoss, sourceName);
        advance(gameData, entry, sourceName, lifeLoss);
    }

    private List<String> availableOptions(GameData gameData, UUID opponentId, int lifeLoss) {
        List<String> options = new ArrayList<>();
        if (!nonlandPermanentIds(gameData, opponentId).isEmpty()) {
            options.add(ChoiceContext.TormentPenaltyChoice.SACRIFICE);
        }
        List<Card> hand = gameData.playerHands.get(opponentId);
        if (hand != null && !hand.isEmpty()) {
            options.add(ChoiceContext.TormentPenaltyChoice.DISCARD);
        }
        options.add("Lose " + lifeLoss + " life");
        return options;
    }

    private List<UUID> nonlandPermanentIds(GameData gameData, UUID opponentId) {
        return destructionSupport.collectPermanentIds(gameData, opponentId,
                p -> !p.getCard().hasType(CardType.LAND));
    }

    /** Opponents of {@code controllerId} in APNAP order (active player first). */
    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex > 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        List<UUID> opponents = new ArrayList<>();
        for (UUID id : rotated) {
            if (!id.equals(controllerId)) {
                opponents.add(id);
            }
        }
        return opponents;
    }
}
