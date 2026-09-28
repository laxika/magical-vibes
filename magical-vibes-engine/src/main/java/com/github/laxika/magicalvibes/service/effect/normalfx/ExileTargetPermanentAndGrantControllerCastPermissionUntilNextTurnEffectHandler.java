package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a targeted exile that grants the spell controller a temporary cast permission. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetPermanentAndGrantControllerCastPermissionUntilNextTurnEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        }

        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }

            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                gameLogService.append(gameData,
                        GameLog.cardThen(entry.getCard(), " fizzles (target no longer on the battlefield)."));
                continue;
            }

            Card exiledCard = target.getOriginalCard();
            permanentRemovalService.removePermanentToExile(gameData, target);
            permanentRemovalService.removeOrphanedAuras(gameData);

            UUID controllerId = entry.getControllerId();
            UUID exileOwnerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
            if (exileOwnerId != null && !exiledCard.hasType(CardType.LAND)) {
                exileSupport.grantPlayUntilNextTurnOfPlayer(
                        gameData, exiledCard.getId(), controllerId, controllerId);
                if (exileEffect.anyManaType()) {
                    gameData.exilePlayAnyManaType.add(exiledCard.getId());
                }
                String controllerName = gameData.playerIdToName.get(controllerId);
                gameLogService.append(gameData, GameLog.builder()
                        .card(exiledCard)
                        .text(" is exiled — " + controllerName
                                + " may cast it until the end of their next turn.")
                        .build());
            } else {
                gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));
            }
            log.info("Game {} - {} exiled by {} (controller may cast until next turn)",
                    gameData.id, exiledCard.getName(), entry.getCard().getName());
        }
    }
}
