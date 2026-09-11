package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
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
        var copyEffect = (CopySpellForEachOtherPermanentOrPlayerEffect) effect;
        UUID targetCardId = copyEffect.spellSnapshot() != null
                ? copyEffect.spellSnapshot().getTargetableId() : entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = copyEffect.spellSnapshot() != null
                ? copyEffect.spellSnapshot()
                : gameData.stack.stream()
                        .filter(stackEntry -> stackEntry.getTargetableId().equals(targetCardId))
                        .findFirst()
                        .orElse(null);
        if (targetEntry == null) return;

        UUID originalTargetId = copyEffect.originalTargetId() != null
                ? copyEffect.originalTargetId()
                : targetEntry.getTargetId();
        if (originalTargetId == null || !isSingleTarget(targetEntry)) return;

        Card spellCard = targetEntry.getCard();
        if (spellCard.isCantBeCopied()) {
            log.info("Game {} - {} can't be copied", gameData.id, spellCard.getName());
            return;
        }

        ValidTargetsResponse validTargets = validTargetService.computeValidTargetsForSpell(
                gameData, spellCard, targetEntry.getControllerId(), List.of(),
                targetEntry.getXValue(), targetEntry.isKicked());
        List<TargetCopy> targets = new ArrayList<>();
        Zone permanentTargetZone = targetEntry.getTargetZone() == Zone.STACK ? Zone.STACK : null;
        validTargets.validPermanentIds().forEach(id -> targets.add(new TargetCopy(id, permanentTargetZone)));
        validTargets.validPlayerIds().forEach(id -> targets.add(new TargetCopy(id, null)));
        validTargets.validGraveyardCardIds().forEach(id -> targets.add(new TargetCopy(id, Zone.GRAVEYARD)));
        validTargets.validExiledCardIds().forEach(id -> targets.add(new TargetCopy(id, Zone.EXILE)));
        targets.removeIf(target -> originalTargetId.equals(target.id()));

        UUID copyControllerId = copyEffect.castingPlayerId() != null
                ? copyEffect.castingPlayerId() : entry.getControllerId();

        for (TargetCopy target : targets) {
            Card copyCard = copySupport.createCopyCard(spellCard);
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    targetEntry, copyCard, copyControllerId, target.id(), target.zone());
            copySupport.addCopyToStack(gameData, copyEntry);

            gameLogService.append(gameData, GameLog.textCardText("A copy of ", spellCard, " is created."));
        }
    }

    private boolean isSingleTarget(StackEntry stackEntry) {
        if (stackEntry.getTargetId() != null && stackEntry.getTargetIds().isEmpty()) {
            return stackEntry.getTargetCardIds().isEmpty()
                    || stackEntry.getTargetCardIds().size() == 1;
        }
        return stackEntry.getTargetId() == null
                && stackEntry.getTargetIds().isEmpty()
                && stackEntry.getTargetCardIds().size() == 1;
    }

    private record TargetCopy(UUID id, Zone zone) {
    }
}
