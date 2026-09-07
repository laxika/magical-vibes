package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellForEachOtherPermanentOrPlayerEffect;
import com.github.laxika.magicalvibes.networking.message.ValidTargetsResponse;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CopySpellForEachOtherPermanentOrPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final ValidTargetService validTargetService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopySpellForEachOtherPermanentOrPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getCard().getId().equals(targetCardId))
                .findFirst()
                .orElse(null);
        if (targetEntry == null || targetEntry.getTargetId() == null
                || !targetEntry.getTargetIds().isEmpty()) return;

        Card spellCard = targetEntry.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        UUID originalTargetId = targetEntry.getTargetId();
        ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForSpell(
                gameData, spellCard, targetEntry.getControllerId(), List.of(),
                targetEntry.getXValue(), targetEntry.isKicked());
        List<UUID> targetIds = new ArrayList<>(validTargets.validPermanentIds());
        targetIds.addAll(validTargets.validPlayerIds());
        targetIds.removeIf(originalTargetId::equals);

        for (UUID targetId : targetIds) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    targetEntry, copyCard, entry.getControllerId(), targetId);
            copySupport.addCopyToStack(gameData, copyEntry);

            gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        }
    }
}
