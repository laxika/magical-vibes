package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentMayPlayWithOpponentTaxEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentMayPlayWithOpponentTaxEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentMayPlayWithOpponentTaxEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exile = (ExileTargetPermanentMayPlayWithOpponentTaxEffect) effect;
        List<UUID> targetIds = entry.targetsForEffect(effect);
        if (targetIds.isEmpty() && entry.getTargetId() != null) {
            targetIds = List.of(entry.getTargetId());
        } else if (targetIds.isEmpty() && entry.getCard().getSpellTargets().size() == 1) {
            targetIds = entry.getTargetIds();
        }

        for (UUID targetId : targetIds) {
            if (targetId == null) {
                continue;
            }

            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            Card exiledCard = target.getOriginalCard();
            permanentRemovalService.removePermanentToExile(gameData, target);

            UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
            if (ownerId != null) {
                if (exile.opponentTax() > 0) {
                    exileSupport.grantPlayWhileExiledWithOpponentTax(
                            gameData, exiledCard.getId(), ownerId, entry.getControllerId(), exile.opponentTax());
                } else {
                    exileSupport.grantPlayWhileExiled(gameData, exiledCard.getId(), ownerId);
                }
                String ownerName = gameData.playerIdToName.get(ownerId);
                String permissionText = " may play it for as long as it remains exiled";
                if (exile.opponentTax() > 0) {
                    permissionText += "; opponents pay {" + exile.opponentTax()
                            + "} more to cast it this way";
                }
                gameLogService.append(gameData, GameLog.builder()
                        .card(exiledCard)
                        .text(" is exiled - " + ownerName + permissionText + ".")
                        .build());
            } else {
                gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));
            }
            log.info("Game {} - {} exiled by {} (owner may play while exiled)",
                    gameData.id, exiledCard.getName(), entry.getCard().getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
