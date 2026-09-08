package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetActivatedOrTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityRetargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves a targeted copy of an activated or triggered ability.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTargetActivatedOrTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTargetActivatedOrTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) {
            return;
        }

        StackEntry targetEntry = null;
        for (StackEntry stackEntry : gameData.stack) {
            if (stackEntry.getCard().getId().equals(targetCardId)
                    && (stackEntry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                    || stackEntry.getEntryType() == StackEntryType.TRIGGERED_ABILITY)) {
                targetEntry = stackEntry;
                break;
            }
        }
        if (targetEntry == null || !entry.getControllerId().equals(targetEntry.getControllerId())) {
            log.info("Game {} - Copy target ability is no longer legal", gameData.id);
            return;
        }

        UUID copyControllerId = targetEntry.getControllerId();
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
                    List.of(new CopyTriggeredAbilityRetargetEffect()),
                    "Choose a new target for the copy of " + targetEntry.getCard().getName() + "'s ability?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }
}
