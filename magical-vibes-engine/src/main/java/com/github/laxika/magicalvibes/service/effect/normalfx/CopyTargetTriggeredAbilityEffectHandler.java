package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTargetTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityRetargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CopyTargetTriggeredAbilityEffect} — puts a copy of the targeted triggered ability
 * onto the stack (CR 707.10). Single-target copies may be retargeted. Used by Strionic Resonator.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTargetTriggeredAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTargetTriggeredAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null) return;

        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)
                    && se.getEntryType() == StackEntryType.TRIGGERED_ABILITY) {
                targetEntry = se;
                break;
            }
        }
        if (targetEntry == null) {
            log.info("Game {} - Copy target triggered ability no longer on stack", gameData.id);
            return;
        }
        if (targetEntry.getCard().isCantBeCopied()) {
            log.info("Game {} - Target triggered ability cannot be copied", gameData.id);
            return;
        }

        // The copy keeps the original source, but is controlled by the effect's controller.
        UUID copyControllerId = entry.getControllerId();
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
                "A copy of ", targetEntry.getCard(), "'s triggered ability is created."));
        log.info("Game {} - copy of {}'s triggered ability created",
                gameData.id, targetEntry.getCard().getName());

        // "You may choose new targets for the copy." Single-target only; multi-target keeps originals.
        boolean singleTarget = targetEntry.getTargetId() != null
                && (targetEntry.getTargetIds() == null || targetEntry.getTargetIds().size() <= 1);
        if (singleTarget && !targetEntry.isNonTargeting()) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(new CopyTriggeredAbilityRetargetEffect()),
                    "Choose a new target for the copy of "
                            + targetEntry.getCard().getName() + "'s triggered ability?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }
}
