package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves The Ruinous Powers' random-opponent top-card exile. */
@Component
@RequiredArgsConstructor
public class ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfRandomOpponentLibraryAndGrantPlayPermissionUntilEndOfTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(entry.getControllerId()))
                .toList();
        if (opponents.isEmpty()) {
            return;
        }

        UUID opponentId = opponents.get(ThreadLocalRandom.current().nextInt(opponents.size()));
        List<Card> library = gameData.playerDecks.get(opponentId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card topCard = library.removeFirst();
        exileService.exileCard(gameData, opponentId, topCard, entry.getSourcePermanentId());
        gameData.exilePlayPermissions.put(topCard.getId(), entry.getControllerId());
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());
        if (!topCard.hasType(CardType.LAND)) {
            gameData.exilePlayAnyManaType.add(topCard.getId());
        }

        String opponentName = gameData.playerIdToName.getOrDefault(opponentId, "that player");
        gameLogService.append(gameData, GameLog.builder()
                .text(entry.getCard().getName() + " exiles ").card(topCard)
                .text(" from " + opponentName + "'s library (may play it this turn).")
                .build());
    }
}
