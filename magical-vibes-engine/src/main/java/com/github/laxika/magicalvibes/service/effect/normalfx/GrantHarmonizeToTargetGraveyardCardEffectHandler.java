package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantHarmonizeToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class GrantHarmonizeToTargetGraveyardCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrantHarmonizeToTargetGraveyardCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = targetCardId(entry);
        if (targetCardId == null) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription() + " — no target selected."));
            return;
        }

        Card targetCard = gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = targetCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || !entry.getControllerId().equals(graveyardOwnerId)
                || (!targetCard.hasType(CardType.INSTANT) && !targetCard.hasType(CardType.SORCERY))) {
            gameLogService.append(gameData, GameLog.text(entry.getDescription()
                    + " fizzles (target is no longer a valid instant or sorcery card in your graveyard)."));
            return;
        }

        gameData.cardsGrantedHarmonizeUntilEndOfTurn.add(targetCardId);
        gameLogService.append(gameData, GameLog.cardTextCard(
                entry.getCard(), " grants harmonize to ", targetCard, " until end of turn."));
        log.info("Game {} - {} grants harmonize to {} until end of turn",
                gameData.id, entry.getCard().getName(), targetCard.getName());
    }

    private UUID targetCardId(StackEntry entry) {
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds != null && !targetCardIds.isEmpty()) {
            return targetCardIds.getFirst();
        }
        return entry.getTargetZone() == Zone.GRAVEYARD ? entry.getTargetId() : null;
    }
}
