package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityFromSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeredAbilityRetargetEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a copy of a triggered ability captured when it was put on the stack. */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyTriggeredAbilityFromSnapshotEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyTriggeredAbilityFromSnapshotEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CopyTriggeredAbilityFromSnapshotEffect copyEffect =
                (CopyTriggeredAbilityFromSnapshotEffect) effect;
        StackEntry snapshot = copyEffect.abilitySnapshot();
        if (snapshot == null) {
            return;
        }

        Card copyCard = copySupport.createCopyCard(snapshot.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(
                snapshot, copyCard, entry.getControllerId(), snapshot.getTargetId());
        copyEntry.setTargetFilter(snapshot.getTargetFilter());
        copyEntry.setDamageSourceCard(snapshot.getDamageSourceCard());
        copyEntry.setTargetGroupSizes(snapshot.getTargetGroupSizes());
        copyEntry.setPrimaryTargetStoredSeparately(snapshot.isPrimaryTargetStoredSeparately());
        copyEntry.setTriggeringPermanentId(snapshot.getTriggeringPermanentId());
        copyEntry.setTriggeringPermanentControllerId(snapshot.getTriggeringPermanentControllerId());
        copyEntry.setTriggeringCardId(snapshot.getTriggeringCardId());
        copyEntry.setEventValue(snapshot.getEventValue());
        copyEntry.setSourcePermanentSnapshot(snapshot.getSourcePermanentSnapshot());
        copyEntry.setNonTargeting(snapshot.isNonTargeting());
        copyEntry.setChosenPermanentId(snapshot.getChosenPermanentId());
        copyEntry.setAttackedTargetId(snapshot.getAttackedTargetId());

        copySupport.addCopyToStack(gameData, copyEntry);
        gameLogService.append(gameData, GameLog.textCardText(
                "A copy of ", snapshot.getCard(), "'s triggered ability is created."));
        log.info("Game {} - copy of {}'s triggered ability created",
                gameData.id, snapshot.getCard().getName());

        boolean singleTarget = snapshot.getTargetId() != null
                && (snapshot.getTargetIds() == null || snapshot.getTargetIds().size() <= 1)
                && !snapshot.isNonTargeting();
        if (singleTarget) {
            gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                    entry.getCard(),
                    entry.getControllerId(),
                    List.of(new CopyTriggeredAbilityRetargetEffect()),
                    "Choose a new target for the copy of "
                            + snapshot.getCard().getName() + "'s triggered ability?",
                    copyCard.getId()));
        }
    }
}
