package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardAtNextEndStepUnlessPays;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentAndReturnAtNextEndStepUnlessPaysEffect.class;
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
            List<Card> cards = target.cardsLeavingBattlefield();
            if (cards.isEmpty() || !permanentRemovalService.removePermanentToExile(gameData, target)) {
                continue;
            }

            Card exiledCard = cards.getFirst();
            gameData.queueDelayedAction(new ReturnExiledCardAtNextEndStepUnlessPays(
                    exiledCard.getId(), entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId()));
            gameLogService.append(gameData, GameLog.cardThen(exiledCard,
                    " is exiled. It will return at the beginning of the next end step unless its controller pays {3}{B}."));
            log.info("Game {} - {} exiles {}; return delayed until next end step unless paid",
                    gameData.id, entry.getCard().getName(), exiledCard.getName());
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
