package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureAndCopyEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.effect.AuraCopyService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureAndCopyEnchantedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final AuraCopyService auraCopyService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureAndCopyEnchantedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        UUID targetId = entry.getTargetId();
        if (sourcePermanentId == null || targetId == null) {
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (source == null || !source.getCard().isAura() || !source.isAttached()) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, source.getAttachedTo());
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (enchanted == null || target == null || !gameQueryService.isCreature(gameData, target)
                || enchanted.getId().equals(target.getId())) {
            return;
        }

        Card copiedCard = target.getCard();
        Card exiledCard = target.getOriginalCard();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID ownerId = gameData.stolenCreatures.getOrDefault(target.getId(), targetControllerId);

        if (!permanentRemovalService.removePermanentToExile(gameData, target, sourcePermanentId)) {
            return;
        }

        if (!exiledCard.isToken()) {
            gameData.addExileReturnOnPermanentLeave(
                    sourcePermanentId, new PendingExileReturn(exiledCard, ownerId));
        }
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData,
                GameLog.cardTextCard(exiledCard, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} until it leaves the battlefield",
                gameData.id, entry.getCard().getName(), exiledCard.getName());

        auraCopyService.applyExiledCreatureCopy(gameData, source, enchanted, copiedCard);
    }
}
