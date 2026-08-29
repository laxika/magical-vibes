package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilTargetEnchantmentLeavesEffect;
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
public class ExileTargetPermanentUntilTargetEnchantmentLeavesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPermanentUntilTargetEnchantmentLeavesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> declaredTargetIds = entry.getDeclaredTargetIds();
        if (declaredTargetIds.size() < 2
                || !entry.isTargetLegal(0)
                || !entry.isTargetLegal(1)) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, declaredTargetIds.get(0));
        Permanent durationEnchantment = gameQueryService.findPermanentById(gameData, declaredTargetIds.get(1));
        UUID controllerId = entry.getControllerId();
        if (target == null || durationEnchantment == null
                || controllerId.equals(gameQueryService.findPermanentController(gameData, target.getId()))
                || !controllerId.equals(gameQueryService.findPermanentController(gameData, durationEnchantment.getId()))
                || (!gameQueryService.isCreature(gameData, target)
                && !gameQueryService.isEnchantment(gameData, target))
                || !gameQueryService.isEnchantment(gameData, durationEnchantment)) {
            return;
        }

        Card card = target.getOriginalCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.defaultControllerOf(target.getId());
        if (ownerId == null) {
            ownerId = card.getOwnerId() != null ? card.getOwnerId() : targetControllerId;
        }

        if (!permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} until the target enchantment leaves the battlefield",
                gameData.id, entry.getCard().getName(), card.getName());

        if (!card.isToken()) {
            gameData.addExileReturnOnPermanentLeave(durationEnchantment.getId(),
                    new PendingExileReturn(card, ownerId));
        }

        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
