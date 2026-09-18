package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Fire Lord Ozai's activated ability. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfEachOpponentLibraryAndAllowOneMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> exiledCardIds = new ArrayList<>();

        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (opponentId.equals(controllerId)) {
                continue;
            }

            List<Card> library = gameData.playerDecks.get(opponentId);
            if (library == null || library.isEmpty()) {
                continue;
            }

            Card card = library.removeFirst();
            exileService.exileCard(gameData, opponentId, card);
            exiledCardIds.add(card.getId());
            gameLogService.append(gameData, GameLog.builder()
                    .text(gameData.playerIdToName.get(opponentId) + " exiles ")
                    .card(card)
                    .text(" from the top of their library.")
                    .build());
        }

        if (exiledCardIds.isEmpty()) {
            return;
        }

        for (UUID cardId : exiledCardIds) {
            gameData.exilePlayPermissions.put(cardId, controllerId);
            gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
            gameData.exilePlayWithoutPayingManaCost.add(cardId);
        }
        gameData.registerExilePlayPermissionGroup(UUID.randomUUID(), 1, exiledCardIds);
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(controllerId)
                        + " may play one of those cards without paying its mana cost this turn."));
    }
}
