package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetPlayerGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutTargetPlayerGraveyardOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetPlayerGraveyardOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            targetPlayerId = entry.targetsForEffect(effect).stream()
                    .filter(gameData.playerIds::contains)
                    .findFirst()
                    .orElse(null);
        }
        if (targetPlayerId == null) {
            return;
        }

        List<Card> moving = new ArrayList<>(graveyardService.takeGraveyardCardsForZoneChange(
                gameData, targetPlayerId));
        Collections.shuffle(moving);
        List<Card> library = gameData.playerDecks.get(targetPlayerId);
        library.addAll(moving);

        String playerName = gameData.playerIdToName.get(targetPlayerId);
        gameLogService.append(gameData, GameLog.text(playerName
                + " puts all cards from their graveyard on the bottom of their library in a random order."));
        log.info("Game {} - {} puts {} graveyard card(s) on the bottom of their library",
                gameData.id, playerName, moving.size());
    }
}
