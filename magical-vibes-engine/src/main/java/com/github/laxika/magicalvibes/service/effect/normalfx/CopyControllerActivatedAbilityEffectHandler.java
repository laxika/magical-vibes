package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyActivatedAbilityRetargetEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
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

    private final GameBroadcastService gameBroadcastService;
    private final CopySupport copySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyControllerActivatedAbilityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (CopyControllerActivatedAbilityEffect) effect;
        StackEntry snapshot = e.abilitySnapshot();
        if (snapshot == null) return;

        UUID copyControllerId = e.activatingPlayerId();
        Card copyCard = copySupport.createCopyCard(snapshot.getCard());
        StackEntry copyEntry = copySupport.createCopyStackEntry(snapshot, copyCard, copyControllerId, snapshot.getTargetId());
        copyEntry.setTargetFilter(snapshot.getTargetFilter());
        copyEntry.setDamageSourceCard(snapshot.getDamageSourceCard());

        gameData.stack.add(copyEntry);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("A copy of ", snapshot.getCard(), "'s ability is created."));
        log.info("Game {} - copy of {}'s ability created for controller", gameData.id, snapshot.getCard().getName());

        // "You may choose new targets for the copy." Only single-target abilities are retargetable here;
        // a copy with no target, or a multi-target ability, keeps the original targets.
        boolean singleTarget = snapshot.getTargetId() != null
                && (snapshot.getTargetIds() == null || snapshot.getTargetIds().size() <= 1)
                && e.ability() != null && !e.ability().isMultiTarget();
        if (singleTarget) {
            PendingMayAbility retargetAbility = new PendingMayAbility(
                    entry.getCard(),
                    copyControllerId,
                    List.of(new CopyActivatedAbilityRetargetEffect(e.ability(), snapshot.getSourcePermanentId())),
                    "Choose a new target for the copy of " + snapshot.getCard().getName() + "'s ability?",
                    copyCard.getId()
            );
            gameData.pendingMayAbilities.addFirst(retargetAbility);
        }
    }
}
