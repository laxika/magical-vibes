package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Resolves Radiant Scrollwielder's upkeep ability. The random choice is made from the matching
 * cards in the controller's graveyard; nonmatching cards remain untouched.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileRandomInstantOrSorceryFromGraveyardMayCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(controllerId, List.of());
        List<Card> candidates = graveyard.stream()
                .filter(card -> card.hasType(CardType.INSTANT) || card.hasType(CardType.SORCERY))
                .toList();
        if (candidates.isEmpty()) {
            return;
        }

        Card exiled = candidates.get(ThreadLocalRandom.current().nextInt(candidates.size()));
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, exiled.getId());
        exileService.exileCard(gameData, controllerId, exiled);

        gameData.exilePlayPermissions.put(exiled.getId(), controllerId);
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiled.getId());
        gameData.exileInsteadOfGraveyard.add(exiled.getId());

        String playerName = gameData.playerIdToName.get(controllerId);
        gameLogService.append(gameData, GameLog.builder()
                .text(playerName + " exiles ").card(exiled)
                .text(" at random from their graveyard and may cast it this turn.")
                .build());
        log.info("Game {} - {} exiles {} at random from their graveyard and may cast it this turn",
                gameData.id, playerName, exiled.getName());
    }
}
