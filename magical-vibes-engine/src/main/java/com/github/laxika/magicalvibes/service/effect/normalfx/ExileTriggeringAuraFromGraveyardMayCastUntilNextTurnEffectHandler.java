package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringAuraFromGraveyardMayCastUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID auraId = entry.getTriggeringCardId();
        if (auraId == null) {
            return;
        }

        UUID graveyardOwnerId = null;
        Card aura = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            aura = graveyard.stream()
                    .filter(card -> auraId.equals(card.getId()))
                    .findFirst()
                    .orElse(null);
            if (aura != null) {
                graveyardOwnerId = playerId;
                break;
            }
        }

        if (aura == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, auraId);
        exileService.exileCard(gameData, graveyardOwnerId, aura);
        exileSupport.grantPlayUntilOwnersNextTurn(gameData, auraId, entry.getControllerId());

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", aura,
                " from the graveyard and grants its controller permission to cast it until the end of their next turn."));
        log.info("Game {} - {} exiles Aura {} and grants cast permission until next turn",
                gameData.id, entry.getCard().getName(), aura.getName());
    }
}
