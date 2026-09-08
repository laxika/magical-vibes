package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
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
public class AirbendSupport {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileSupport exileSupport;

    public void airbend(GameData gameData, StackEntry entry, Permanent target) {
        List<Card> exiledCards = target.cardsLeavingBattlefield();
        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        for (Card exiledCard : exiledCards) {
            UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
            if (ownerId != null) {
                exileSupport.grantCastWhileExiledForGenericCost(gameData, exiledCard.getId(), ownerId, 2);
                String ownerName = gameData.playerIdToName.get(ownerId);
                gameLogService.append(gameData, GameLog.builder()
                        .card(exiledCard)
                        .text(" is airbent - " + ownerName
                                + " may cast it for {2} rather than its mana cost while it remains exiled.")
                        .build());
            } else {
                gameLogService.append(gameData, GameLog.cardThen(exiledCard, " is airbent."));
            }
            log.info("Game {} - {} airbent by {} (owner may cast for {2})",
                    gameData.id, exiledCard.getName(), entry.getCard().getName());
        }
    }
}
