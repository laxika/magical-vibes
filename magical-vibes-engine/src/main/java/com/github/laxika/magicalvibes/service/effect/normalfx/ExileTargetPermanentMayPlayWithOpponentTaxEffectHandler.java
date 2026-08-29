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
        UUID targetId = entry.getTargetId();
        if (targetId == null && !entry.getTargetIds().isEmpty()) {
            targetId = entry.getTargetIds().getFirst();
        }
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), " fizzles (target no longer on the battlefield)."));
            return;
        }

        Card exiledCard = target.getOriginalCard();
        permanentRemovalService.removePermanentToExile(gameData, target);
        permanentRemovalService.removeOrphanedAuras(gameData);

        UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
        if (ownerId != null) {
            exileSupport.grantPlayWhileExiledWithOpponentTax(
                    gameData, exiledCard.getId(), ownerId, entry.getControllerId(), 2);
            String ownerName = gameData.playerIdToName.get(ownerId);
            gameLogService.append(gameData, GameLog.builder()
                    .card(exiledCard)
                    .text(" is exiled — " + ownerName
                            + " may play it for as long as it remains exiled; opponents pay {2} more to cast it this way.")
                    .build());
        } else {
            gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is exiled."));
        }
        log.info("Game {} - {} exiled by {} (owner may play while exiled with opponent tax)",
                gameData.id, exiledCard.getName(), entry.getCard().getName());
    }
}
