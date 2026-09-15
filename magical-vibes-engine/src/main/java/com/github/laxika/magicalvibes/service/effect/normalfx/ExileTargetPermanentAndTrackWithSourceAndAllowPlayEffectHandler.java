package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect;
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
public class ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndTrackWithSourceAndAllowPlayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
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
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(entry.getControllerId());
                if (battlefield != null) {
                    for (Permanent permanent : battlefield) {
                        if (permanent.getCard() == entry.getCard()) {
                            sourcePermanentId = permanent.getId();
                            break;
                        }
                    }
                }
            }

            if (sourcePermanentId != null) {
                exileSupport.exilePermanentAndTrackWithSource(
                        gameData, target, sourcePermanentId, entry.getCard());
            } else {
                exileSupport.exilePermanentAndLog(gameData, target, entry.getCard().getName());
            }

            UUID ownerId = gameQueryService.findExileOwnerById(gameData, exiledCard.getId());
            if (ownerId != null) {
                exileSupport.grantPlayWhileExiled(gameData, exiledCard.getId(), ownerId);
                gameLogService.append(gameData, GameLog.builder()
                        .card(exiledCard)
                        .text("'s owner may play it for as long as it remains exiled.")
                        .build());
            }
            log.info("Game {} - {} exiled by {} and its owner may play it while exiled",
                    gameData.id, exiledCard.getName(), entry.getCard().getName());
        }
    }
}
