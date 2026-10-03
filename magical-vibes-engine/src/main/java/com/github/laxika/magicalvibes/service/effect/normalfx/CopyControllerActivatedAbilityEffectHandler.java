package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopyActivatedAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link CopyControllerActivatedAbilityEffect} — creates a copy of the snapshotted
 * activated ability on the stack (CR 707.10) for its controller. If the ability had a single
 * target, the controller may choose a new target for the copy. Used by Rings of Brighthearth.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CopyControllerActivatedAbilityEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final CopySupport copySupport;
    private final PsychicBattleSupport psychicBattleSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyControllerActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopyControllerActivatedAbilityEffect) effect;
        StackEntry snapshot = e.abilitySnapshot();
        if (snapshot == null || snapshot.getCard().isCantBeCopied()) return;

        UUID copyControllerId = e.activatingPlayerId();
        Card copyCard = copySupport.createCopyCard(snapshot.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(snapshot, copyCard, copyControllerId, snapshot.getTargetId());
        copyEntry.setTargetFilter(snapshot.getTargetFilter());
        copyEntry.setDamageSourceCard(snapshot.getDamageSourceCard());
        copyEntry.setNonTargeting(snapshot.isNonTargeting());

        copySupport.addCopyToStack(gameData, copyEntry);

        gameLogService.append(gameData, GameLog.textCardText("A copy of ", snapshot.getCard(), "'s ability is created."));
        log.info("Game {} - copy of {}'s ability created for controller", gameData.id, snapshot.getCard().getName());

        if (!snapshot.isNonTargeting() && psychicBattleSupport.targetIds(copyEntry).size() > 1) {
            psychicBattleSupport.queueNextChoice(gameData, entry.getCard(), copyControllerId,
                    copyEntry.getTargetableId(), 0);
            return;
        }
        boolean singleTarget = snapshot.getTargetId() != null
                && (snapshot.getTargetIds() == null || snapshot.getTargetIds().size() <= 1)
                && !snapshot.isNonTargeting()
                && (e.ability() == null || !e.ability().isMultiTarget());
        if (singleTarget) {
            CardEffect retargetEffect = e.ability() == null
                    ? new CopyAbilityRetargetEffect()
                    : new CopyActivatedAbilityRetargetEffect(e.ability(), snapshot.getSourcePermanentId());
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(retargetEffect),
                    "Choose a new target for the copy of " + snapshot.getCard().getName() + "'s ability?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }
}
