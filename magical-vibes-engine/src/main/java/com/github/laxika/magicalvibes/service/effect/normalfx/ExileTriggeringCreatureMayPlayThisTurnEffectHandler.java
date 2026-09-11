package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureMayPlayThisTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureMayPlayThisTurnEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureMayPlayThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringPermanentId = entry.getTriggeringPermanentId() != null
                ? entry.getTriggeringPermanentId()
                : entry.getSourcePermanentId();
        if (triggeringPermanentId == null) {
            return;
        }

        Permanent creature = gameQueryService.findPermanentById(gameData, triggeringPermanentId);
        if (creature == null) {
            return;
        }

        Card exiledCard = creature.getOriginalCard();
        if (!permanentRemovalService.removePermanentToExile(gameData, creature)) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        if (exiledCard.isToken()) {
            gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));
            return;
        }

        UUID exileOwnerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
        if (exileOwnerId == null) {
            gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));
            return;
        }

        gameData.exilePlayPermissions.put(exiledCard.getId(), entry.getControllerId());
        gameData.exilePlayPermissionsExpireEndOfTurn.add(exiledCard.getId());
        gameLogService.append(gameData, GameLog.cardThen(exiledCard,
                " is exiled; the ability's controller may play it this turn."));
        log.info("Game {} - {} exiles {} and may play it this turn",
                gameData.id, gameData.playerIdToName.get(entry.getControllerId()), exiledCard.getName());
    }
}
