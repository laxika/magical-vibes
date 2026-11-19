package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AnteTopCardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves the mandatory ante of the top library card of each player {@link SubjectResolutionService} finds for the
 * effect's subject, in APNAP order.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AnteTopCardEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AnteTopCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        AnteTopCardEffect anteEffect = (AnteTopCardEffect) effect;
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, anteEffect.player())) {
            anteTopCard(gameData, entry, playerId);
        }
    }

    private void anteTopCard(GameData gameData, StackEntry entry, UUID playerId) {
        List<Card> library = gameData.playerDecks.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        Card anted = library.removeFirst();
        gameData.addToAnte(playerId, anted);
        gameLogService.append(gameData,
                GameLog.builder()
                        .text(gameData.playerIdToName.get(playerId) + " antes ")
                        .card(anted)
                        .text(". (")
                        .card(entry.getCard())
                        .text(")")
                        .build());
        log.info("Game {} - {} antes {} to {}", gameData.id,
                gameData.playerIdToName.get(playerId), anted.getName(), entry.getCard().getName());
    }
}
