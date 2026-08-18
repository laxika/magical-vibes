package com.github.laxika.magicalvibes.service.epic;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.EpicDelayedTrigger;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.CopySupport;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/** Resolves Epic's game-long restriction and recurring upkeep spell copies. */
@Slf4j
@Service
@RequiredArgsConstructor
public class EpicService {

    private final CopySupport copySupport;
    private final GameLogService gameLogService;

    public void register(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        gameData.playersCantCastSpellsForRestOfGame.add(controllerId);
        Card prototype = copySupport.createCopyCardWithoutEpic(entry.getCard());
        gameData.queueDelayedAction(new EpicDelayedTrigger(controllerId, prototype, entry.getTargetId()));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s Epic ability is registered."));
        log.info("Game {} - {} registers Epic for {}", gameData.id, entry.getCard().getName(), controllerId);
    }

    public void fireUpkeepTriggers(GameData gameData) {
        UUID activePlayerId = gameData.activePlayerId;
        for (EpicDelayedTrigger trigger : gameData.getDelayedActions(EpicDelayedTrigger.class)) {
            if (!trigger.controllerId().equals(activePlayerId)) continue;

            Card copyCard = copySupport.createCopyCardWithoutEpic(trigger.spellPrototype());
            StackEntry copy = new StackEntry(
                    StackEntryType.SORCERY_SPELL,
                    copyCard,
                    activePlayerId,
                    "Copy of " + copyCard.getName(),
                    new ArrayList<>(copyCard.getEffects(EffectSlot.SPELL)));
            copy.setCopy(true);
            copy.setTargetId(trigger.targetId());
            copy.setTargetFilter(copyCard.getTargetFilter());
            copy.setNonTargeting(!EffectResolution.needsTarget(copyCard)
                    && !EffectResolution.needsSpellTarget(copyCard));
            gameData.stack.add(copy);
            if (copy.getTargetId() != null) {
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        copyCard,
                        activePlayerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + copyCard.getName() + "?",
                        copyCard.getId()));
            }
            gameLogService.append(gameData, GameLog.textCardText("A copy of ", copyCard, " is put onto the stack by Epic."));
            log.info("Game {} - {} Epic copy pushed onto the stack", gameData.id, copyCard.getName());
        }
    }
}
