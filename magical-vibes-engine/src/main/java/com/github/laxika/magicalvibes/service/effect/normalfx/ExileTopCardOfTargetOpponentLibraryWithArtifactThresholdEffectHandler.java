package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffect;
import com.github.laxika.magicalvibes.model.effect.MayPlayExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopCardOfTargetOpponentLibraryWithArtifactThresholdEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID controllerId = entry.getControllerId();
        List<Card> deck = targetPlayerId == null ? null : gameData.playerDecks.get(targetPlayerId);

        if (deck == null || deck.isEmpty()) {
            createTreasure(gameData, entry);
            return;
        }

        Card topCard = deck.removeFirst();
        exileService.exileCard(gameData, targetPlayerId, topCard);
        createTreasure(gameData, entry);

        if (topCard.hasType(CardType.LAND)) {
            logExile(gameData, targetPlayerId, topCard);
            return;
        }

        gameData.exilePlayPermissions.put(topCard.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(topCard.getId());

        int artifactCount = countArtifacts(gameData, controllerId);
        if (topCard.getManaValue() < artifactCount) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    controllerId,
                    List.of(new MayPlayExiledCardWithoutPayingManaCostEffect()),
                    "Cast " + topCard.getName() + " without paying its mana cost?",
                    topCard.getId()));
        }

        logExile(gameData, targetPlayerId, topCard);
    }

    private void createTreasure(GameData gameData, StackEntry entry) {
        createTokenEffectHandler.resolve(gameData, entry, CreateTokenEffect.ofTreasureToken(1));
    }

    private int countArtifacts(GameData gameData, UUID controllerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) {
            return 0;
        }
        return (int) battlefield.stream()
                .filter(permanent -> gameQueryService.isArtifact(gameData, permanent))
                .count();
    }

    private void logExile(GameData gameData, UUID targetPlayerId, Card topCard) {
        String targetName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.textCardText(targetName + " exiles ", topCard,
                " from the top of their library."));
        log.info("Game {} - {} exiles {} from library top", gameData.id, targetName, topCard.getName());
    }
}
