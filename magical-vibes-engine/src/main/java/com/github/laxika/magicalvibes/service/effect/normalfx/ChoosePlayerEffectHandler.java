package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerEffect;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves {@link ChoosePlayerEffect}: a step that pauses at its end, the choice recording what it chose. */
@Component
@RequiredArgsConstructor
public class ChoosePlayerEffectHandler implements NormalEffectHandlerBean {

    private final SubjectResolutionService subjectResolutionService;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChoosePlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var choose = (ChoosePlayerEffect) effect;
        List<UUID> players = subjectResolutionService.players(gameData, entry, effect, choose.players());
        entry.recordStep(StepKind.CHOOSE, StepOutcome.NONE);
        UUID chooserId = choose.chooser() == null ? entry.getControllerId()
                : subjectResolutionService.player(gameData, entry, effect, choose.chooser());
        if (choose.chooser() != null && (chooserId == null || !gameData.playerIds.contains(chooserId))) {
            // The chooser is gone (a player who left the game): nobody chooses, and nothing is chosen.
            return;
        }
        if (players.size() == 1) {
            record(gameData, entry, players.getFirst());
        } else if (players.size() > 1) {
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.RecordChosenPlayer(players));
            playerInputService.beginPlayerChoice(gameData, chooserId, players,
                    FaceDownVisibility.nameShownTo(gameData, entry, chooserId) + " — choose " + choose.label() + ".");
        }
    }

    /**
     * Whether the opponent an instruction about "an opponent" acts on is still to be chosen by the controller
     * ("clash with an opponent", "fateseal N", "an opponent chooses"; CR 701.30b): with two or more opponents the
     * choice ({@link ChoosePlayerEffect#opponent()}) and {@code rerun} are put next in {@code entry}'s resolution,
     * {@code rerun} reading the chosen player as {@code playersThisWay(CHOOSE)}, and the caller returns before it
     * changes anything, so the instruction resolves once, after the choice. With one opponent nothing is asked
     * (CR 608.2d): {@link #onlyOpponent(GameData, UUID)} is that player.
     */
    public static boolean opponentToBeChosenFirst(GameData gameData, StackEntry entry, CardEffect rerun) {
        if (opponents(gameData, entry.getControllerId()).size() <= 1) {
            return false;
        }
        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                List.of(ChoosePlayerEffect.opponent(), rerun));
        return true;
    }

    /** {@code controllerId}'s one opponent still in the game, or {@code null} when there is none. */
    public static UUID onlyOpponent(GameData gameData, UUID controllerId) {
        List<UUID> opponents = opponents(gameData, controllerId);
        return opponents.isEmpty() ? null : opponents.getFirst();
    }

    private static List<UUID> opponents(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId) && gameData.playerIds.contains(playerId))
                .toList();
    }

    /** The answer to the choice. */
    public void completeChoice(GameData gameData, UUID playerId, PermanentChoiceContext.RecordChosenPlayer context) {
        if (!context.playerIds().contains(playerId)) {
            throw new IllegalStateException("Invalid player choice");
        }
        record(gameData, gameData.pendingEffectResolutionEntry, playerId);
    }

    private void record(GameData gameData, StackEntry entry, UUID playerId) {
        entry.recordStep(StepKind.CHOOSE, StepOutcome.ofAmount(1, List.of(playerId)));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                ": " + gameData.playerIdToName.get(playerId) + " is chosen."));
    }
}
