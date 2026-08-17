package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.DispersalState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Dispersal's opponent-by-opponent return-then-discard sequence. */
@Component
@RequiredArgsConstructor
public class EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentReturnsGreatestManaValueNonlandPermanentThenDiscardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DispersalState state = gameData.dispersal;
        if (!state.active) {
            state.reset();
            state.active = true;
            state.remainingOpponentIds.addAll(apnapOpponents(gameData, entry.getControllerId()));
        }

        if (state.awaitingDiscard) {
            state.awaitingDiscard = false;
            state.currentOpponentId = null;
        }

        while (true) {
            if (state.selectedPermanentId != null) {
                Permanent selected = gameQueryService.findPermanentById(gameData, state.selectedPermanentId);
                state.selectedPermanentId = null;
                if (selected != null && permanentRemovalService.removePermanentToHand(gameData, selected)) {
                    gameLogService.append(gameData,
                            GameLog.cardThen(selected.getCard(), " is returned to its owner's hand."));
                }
                if (beginDiscard(gameData, entry, state)) {
                    return;
                }
                state.currentOpponentId = null;
                continue;
            }

            if (state.currentOpponentId == null) {
                if (state.remainingOpponentIds.isEmpty()) {
                    state.reset();
                    gameData.rerunCurrentEffectAfterInteraction = false;
                    return;
                }

                state.currentOpponentId = state.remainingOpponentIds.removeFirst();
                List<Permanent> tied = greatestManaValueNonlands(gameData, state.currentOpponentId);
                if (tied.isEmpty()) {
                    if (beginDiscard(gameData, entry, state)) {
                        return;
                    }
                    state.currentOpponentId = null;
                    continue;
                }

                if (tied.size() > 1) {
                    gameData.rerunCurrentEffectAfterInteraction = true;
                    gameData.interaction.setPermanentChoiceContext(
                            new PermanentChoiceContext.DispersalTieBreak(
                                    state.currentOpponentId, entry.getCard()));
                    playerInputService.beginPermanentChoice(gameData, state.currentOpponentId,
                            tied.stream().map(Permanent::getId).toList(),
                            "Choose a nonland permanent with the greatest mana value to return ("
                                    + entry.getCard().getName() + ").");
                    return;
                }

                state.selectedPermanentId = tied.getFirst().getId();
                continue;
            }

            if (beginDiscard(gameData, entry, state)) {
                return;
            }
            state.currentOpponentId = null;
        }
    }

    /** Completes the current opponent's tied-permanent choice and resumes the effect. */
    public void handleTieBreakChosen(GameData gameData, UUID permanentId,
            PermanentChoiceContext.DispersalTieBreak context) {
        if (!gameData.dispersal.active
                || !context.playerId().equals(gameData.dispersal.currentOpponentId)) {
            throw new IllegalStateException("No pending Dispersal tie-break");
        }
        gameData.dispersal.selectedPermanentId = permanentId;
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean beginDiscard(GameData gameData, StackEntry entry, DispersalState state) {
        UUID opponentId = state.currentOpponentId;
        List<Card> hand = gameData.playerHands.get(opponentId);
        if (hand == null || hand.isEmpty()) {
            return false;
        }

        gameData.discardCausedByOpponent = !opponentId.equals(entry.getControllerId());
        state.awaitingDiscard = true;
        gameData.rerunCurrentEffectAfterInteraction = true;
        playerInteractionSupport.resolveDiscardCards(gameData, opponentId, 1, DiscardFollowUp.NONE);
        return true;
    }

    private List<Permanent> greatestManaValueNonlands(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return List.of();
        }

        List<Permanent> eligible = battlefield.stream()
                .filter(permanent -> !gameQueryService.isLand(gameData, permanent))
                .toList();
        int greatestManaValue = eligible.stream()
                .mapToInt(permanent -> permanent.getCard().getManaValue())
                .max()
                .orElse(Integer.MIN_VALUE);
        return eligible.stream()
                .filter(permanent -> permanent.getCard().getManaValue() == greatestManaValue)
                .toList();
    }

    private List<UUID> apnapOpponents(GameData gameData, UUID controllerId) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        List<UUID> rotated = new ArrayList<>();
        if (activeIndex >= 0) {
            rotated.addAll(ordered.subList(activeIndex, ordered.size()));
            rotated.addAll(ordered.subList(0, activeIndex));
        } else {
            rotated.addAll(ordered);
        }
        return rotated.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
