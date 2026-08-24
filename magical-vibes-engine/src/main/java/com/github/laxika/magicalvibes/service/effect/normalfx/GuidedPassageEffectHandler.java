package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GuidedPassageEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Guided Passage's full-library reveal and opponent choice. */
@Component
@RequiredArgsConstructor
public class GuidedPassageEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GuidedPassageEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library == null || library.isEmpty()) {
            shuffleLibrary(gameData, controllerId);
            return;
        }

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
        if (opponents.isEmpty()) {
            shuffleLibrary(gameData, controllerId);
            return;
        }

        if (opponents.size() == 1) {
            beginCardChoice(gameData, controllerId, opponents.getFirst(), library);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.GuidedPassageOpponentChoice(controllerId, library));
        playerInputService.beginAnyTargetChoice(gameData, controllerId, List.of(), opponents,
                "Guided Passage — choose an opponent to choose cards.");
    }

    public void completeOpponentChoice(GameData gameData, UUID opponentId,
            PermanentChoiceContext.GuidedPassageOpponentChoice choice) {
        beginCardChoice(gameData, choice.controllerId(), opponentId, choice.library());
    }

    private void beginCardChoice(GameData gameData, UUID controllerId, UUID opponentId,
            List<Card> library) {
        GameLog.Builder revealBuilder = GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId)
                        + " reveals their library with Guided Passage: ");
        for (int i = 0; i < library.size(); i++) {
            if (i > 0) {
                revealBuilder.text(", ");
            }
            revealBuilder.card(library.get(i));
        }
        revealBuilder.text(".");
        gameLogService.append(gameData, revealBuilder.build());
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.GuidedPassageChoice(opponentId, controllerId, library));
    }

    private void shuffleLibrary(GameData gameData, UUID playerId) {
        LibraryShuffleHelper.shuffleLibrary(gameData, playerId);
    }
}
