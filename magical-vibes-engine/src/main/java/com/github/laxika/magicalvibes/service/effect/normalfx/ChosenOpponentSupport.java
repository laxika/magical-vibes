package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChosenOpponentFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * "An opponent" named in the middle of a resolution, after an exile, a search, a reveal or a pile choice: the
 * controller chooses that opponent as the instruction is applied (CR 608.2d), not before the resolution starts
 * ({@link ChoosePlayerEffectHandler#opponentToBeChosenFirst} is for an instruction that changes nothing first).
 */
@Component
@RequiredArgsConstructor
public class ChosenOpponentSupport {

    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final ObjectProvider<ChosenOpponentContinuation<?>> continuations;

    /**
     * Goes on with {@code followUp} once {@code controllerId}'s opponent is known: with two or more opponents the
     * controller is asked (the answer comes back through {@link #complete}); with one, or none, nothing is asked and
     * the flow goes on at once with that opponent or {@code null}. The caller changes nothing after this call.
     */
    public void chooseThen(GameData gameData, UUID controllerId, String sourceName, ChosenOpponentFollowUp followUp) {
        List<UUID> opponents = ChoosePlayerEffectHandler.opponents(gameData, controllerId);
        if (opponents.size() <= 1) {
            continueWith(gameData, opponents.isEmpty() ? null : opponents.getFirst(), followUp);
            return;
        }
        askThen(gameData, controllerId, opponents, sourceName + " — choose an opponent.", followUp);
    }

    /**
     * Asks {@code chooserId} to choose one of {@code opponents} (two or more, the ones the instruction can act on) and
     * goes on with {@code followUp} once answered ({@link #complete}). The caller changes nothing after this call.
     */
    public void askThen(GameData gameData, UUID chooserId, List<UUID> opponents, String prompt,
                        ChosenOpponentFollowUp followUp) {
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ChooseOpponentThen(followUp));
        playerInputService.beginPlayerChoice(gameData, chooserId, opponents, prompt);
    }

    /** The controller's answer: the chosen opponent goes on with the follow-up. */
    public void complete(GameData gameData, UUID opponentId, PermanentChoiceContext.ChooseOpponentThen context) {
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(opponentId) + " is chosen."));
        continueWith(gameData, opponentId, context.followUp());
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private void continueWith(GameData gameData, UUID opponentId, ChosenOpponentFollowUp followUp) {
        ChosenOpponentContinuation continuation = continuations.stream()
                .filter(candidate -> candidate.followUpType().isInstance(followUp))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No continuation for " + followUp.getClass()));
        continuation.continueWithOpponent(gameData, opponentId, followUp);
    }
}
