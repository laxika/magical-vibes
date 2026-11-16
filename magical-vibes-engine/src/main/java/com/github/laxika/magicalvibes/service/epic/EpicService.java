package com.github.laxika.magicalvibes.service.epic;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.EpicUpkeepCopyEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
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
        // "At the beginning of each of your upkeeps for the rest of the game, copy this spell": a delayed triggered
        // ability the spell made as it resolved, made by the spell and controlled by its controller (CR 702.50a,
        // 603.7e), that triggers at every one of their upkeeps (CR 603.7b).
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atEveryStep(controllerId,
                prototype, new TriggeredAbility(Triggers.upkeep(ActivePlayer.YOU),
                new EpicUpkeepCopyEffect(entry.getTargetId())), null, null, DelayedReferents.NONE, null));
        gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s Epic ability is registered."));
        log.info("Game {} - {} registers Epic for {}", gameData.id, entry.getCard().getName(), controllerId);
    }

    public void createUpkeepCopy(GameData gameData, UUID controllerId, Card prototype, UUID targetId) {
        Card copyCard = copySupport.createCopyCardWithoutEpic(prototype);
        StackEntry copy = new StackEntry(
                StackEntryType.SORCERY_SPELL,
                copyCard,
                controllerId,
                "Copy of " + copyCard.getName(),
                new ArrayList<>(copyCard.getCastingCostsAndSpellEffects()));
        copy.setCopy(true);
        copy.setTargetId(targetId);
        copy.setTargetFilter(copyCard.getTargetFilter());
        copy.setNonTargeting(!EffectResolution.needsTarget(copyCard)
                && !EffectResolution.needsSpellTarget(copyCard));
        copySupport.addCopyToStack(gameData, copy, copy.getTargetId() == null ? null
                : new PendingMayAbility(
                        copyCard,
                        controllerId,
                        List.of(new CopySpellEffect()),
                        "Choose new targets for the copy of " + copyCard.getName() + "?",
                        copyCard.getId()));
        gameLogService.append(gameData, GameLog.textCardText("A copy of ", copyCard, " is put onto the stack by Epic."));
        log.info("Game {} - {} Epic copy pushed onto the stack", gameData.id, copyCard.getName());
    }
}
