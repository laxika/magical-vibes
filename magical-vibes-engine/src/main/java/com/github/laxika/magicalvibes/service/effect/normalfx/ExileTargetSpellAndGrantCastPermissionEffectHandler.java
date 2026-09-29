package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetSpellAndGrantCastPermissionEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves target-spell exile with a persistent cast permission for the effect controller. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetSpellAndGrantCastPermissionEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;
    private final StateTriggerService stateTriggerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetSpellAndGrantCastPermissionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry target = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (target == null) {
            log.info("Game {} - {}'s exile target is no longer on the stack",
                    gameData.id, entry.getCard().getName());
            return;
        }

        gameData.stack.remove(target);
        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (target.isCopy()) {
            gameLogService.append(gameData, GameLog.cardThen(target.getCard(), " (a copy) ceases to exist."));
            return;
        }

        Card card = target.getPhysicalCard();
        exileService.exileCard(gameData, target.getOwnerId(), card);
        exileSupport.grantPlayWhileExiled(gameData, card.getId(), entry.getControllerId());

        ExileTargetSpellAndGrantCastPermissionEffect permissionEffect =
                (ExileTargetSpellAndGrantCastPermissionEffect) effect;
        if (permissionEffect.withoutPayingManaCost()) {
            gameData.exilePlayWithoutPayingManaCost.add(card.getId());
        } else {
            gameData.exilePlayAnyManaTypeWhileExiled.add(card.getId());
        }

        gameLogService.append(gameData, GameLog.cardTextCard(card,
                " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiled {} and granted cast permission to {}",
                gameData.id, entry.getCard().getName(), card.getName(),
                gameData.playerIdToName.get(entry.getControllerId()));
    }
}
