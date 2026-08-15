package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingDubiousChallengeChoice;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DubiousChallengeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DubiousChallengeEffectHandler implements NormalEffectHandlerBean {

    private static final int LOOK_COUNT = 10;
    private static final int MAX_EXILED_CREATURES = 2;

    private final GameLogService gameLogService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DubiousChallengeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID opponentId = entry.getTargetId();
        List<Card> deck = gameData.playerDecks.getOrDefault(controllerId, List.of());
        int actualLookCount = Math.min(LOOK_COUNT, deck.size());

        if (actualLookCount == 0) {
            return;
        }

        List<Card> lookedAt = new ArrayList<>(deck.subList(0, actualLookCount));
        List<UUID> creatureIds = lookedAt.stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .map(Card::getId)
                .toList();
        if (creatureIds.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        String opponentName = gameData.playerIdToName.get(opponentId);
        gameData.queueInteraction(new PendingDubiousChallengeChoice(controllerId, opponentId, List.of()));
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryRevealChoice(
                controllerId, lookedAt, creatureIds, false, false, false, false, false,
                0, null, MAX_EXILED_CREATURES,
                "Choose up to two creature cards to exile for Dubious Challenge.", 0, false));
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId) + " looks at the top " + actualLookCount
                        + " cards of their library for Dubious Challenge. " + opponentName
                        + " will choose from the exiled cards."));
    }
}
