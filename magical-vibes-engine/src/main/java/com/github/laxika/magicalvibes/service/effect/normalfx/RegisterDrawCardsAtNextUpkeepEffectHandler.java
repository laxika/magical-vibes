package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDrawCardsAtNextUpkeepEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDrawCardsAtNextUpkeepEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDrawCardsAtNextUpkeepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDrawCardsAtNextUpkeepEffect) effect;
        UUID drawerId = switch (e.recipient()) {
            case CONTROLLER -> entry.getControllerId();
            case TARGET_PLAYER -> entry.getTargetId();
            case TARGET_SPELL_CONTROLLER -> findTargetSpellControllerId(gameData, entry.getTargetId());
            case TARGET_GRAVEYARD_OWNER -> findTargetGraveyardOwnerId(gameData, entry);
        };
        if (drawerId == null) {
            return;
        }
        gameData.queueDelayedAction(new DrawCardsAtNextUpkeep(drawerId, e.count(), entry.getCard(), e.upTo(),
                gameData.turnNumber, gameData.activePlayerId));

        String playerName = gameData.playerIdToName.get(drawerId);
        log.info("Game {} - {} registers delayed draw of {} at next upkeep", gameData.id, playerName, e.count());
    }

    /**
     * Owner of the graveyard holding this entry's first still-present targeted card, or null when the
     * entry has no graveyard targets left (Lodestone Bauble activated with no targets — nobody draws).
     */
    private UUID findTargetGraveyardOwnerId(GameData gameData, StackEntry entry) {
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds == null) {
            return null;
        }
        for (UUID cardId : targetCardIds) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            if (ownerId != null) {
                return ownerId;
            }
        }
        return null;
    }

    /** Controller of the spell on the stack whose card id matches {@code targetCardId}, or null. */
    private UUID findTargetSpellControllerId(GameData gameData, UUID targetCardId) {
        if (targetCardId == null) {
            return null;
        }
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                return se.getControllerId();
            }
        }
        return null;
    }
}
