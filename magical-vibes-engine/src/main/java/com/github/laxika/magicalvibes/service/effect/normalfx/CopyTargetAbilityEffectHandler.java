package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves {@link CopyTargetAbilityEffect} by creating the requested number of copies. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTargetAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTargetAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null || entry.getXValue() <= 0) {
            return;
        }

        StackEntry targetEntry = null;
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)
                    && Set.of(StackEntryType.ACTIVATED_ABILITY, StackEntryType.TRIGGERED_ABILITY)
                    .contains(stackEntry.getEntryType())) {
                targetEntry = stackEntry;
                break;
            }
        }
        if (targetEntry == null) {
            log.info("Game {} - Copy target ability no longer on stack", gameData.id);
            return;
        }
        if (targetEntry.getCard().isCantBeCopied()) {
            log.info("Game {} - Target ability cannot be copied", gameData.id);
            return;
        }

        UUID copyControllerId = entry.getControllerId();
        for (int i = 0; i < entry.getXValue(); i++) {
            Card copyCard = copySupport.createCopyCard(targetEntry.getCard());
            StackEntry copyEntry = copySupport.createCopyStackEntry(
                    targetEntry, copyCard, copyControllerId, targetEntry.getTargetId());
            copyEntry.setTargetFilter(targetEntry.getTargetFilter());
            copyEntry.setDamageSourceCard(targetEntry.getDamageSourceCard());
            copyEntry.setTriggeringPermanentId(targetEntry.getTriggeringPermanentId());
            copyEntry.setTriggeringPermanentControllerId(targetEntry.getTriggeringPermanentControllerId());
            copyEntry.setTriggeringCardId(targetEntry.getTriggeringCardId());
            copyEntry.setEventValue(targetEntry.getEventValue());
            copyEntry.setSourcePermanentSnapshot(targetEntry.getSourcePermanentSnapshot());
            copyEntry.setNonTargeting(targetEntry.isNonTargeting());
            copyEntry.setChosenPermanentId(targetEntry.getChosenPermanentId());
            copyEntry.setAttackedTargetId(targetEntry.getAttackedTargetId());

            copySupport.addCopyToStack(gameData, copyEntry);

            gameLogService.append(gameData, GameLog.textCardText(
                    "A copy of ", targetEntry.getCard(), "'s ability is created."));
            log.info("Game {} - copy of {}'s ability created", gameData.id, targetEntry.getCard().getName());

            boolean singleTarget = targetEntry.getTargetId() != null
                    && (targetEntry.getTargetIds() == null || targetEntry.getTargetIds().size() <= 1)
                    && !targetEntry.isNonTargeting();
            if (singleTarget) {
                PendingMayAbility retargetAbility = new PendingMayAbility(
                        entry.getCard(),
                        copyControllerId,
                        List.of(new CopyAbilityRetargetEffect()),
                        "Choose a new target for the copy of "
                                + targetEntry.getCard().getName() + "'s ability?",
                        copyCard.getId());
                gameData.pendingMayAbilities.addFirst(retargetAbility);
            }
        }
    }
}
