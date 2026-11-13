package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.MiscWatch;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Hands the "whenever this Equipment becomes unattached from a permanent" abilities of an Equipment that became
 * unattached to the stack door ({@code Triggers.becomesUnattachedFromPermanent()}: Captain's Hook, Grafted Wargear,
 * Grafted Exoskeleton, Stitcher's Graft). Each ability is one object of all its effects (CR 603.3) and records the
 * permanent the Equipment was attached to as its triggering permanent, "that permanent", whatever its effects are, so
 * the permanent stays on the battlefield until the ability resolves. The ability is the Equipment's as it was just
 * before it became unattached (CR 603.10c): a face-down Equipment, or one that had lost its abilities (CR 613.1f),
 * has no such ability, and nothing triggers (CR 603.2).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnattachTriggerSupport {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    public void triggerDestroyOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                 UUID oldAttachedTo) {
        triggerDestroyOnUnattachIfNeeded(gameData, equipment, oldAttachedTo,
                gameData.findControllerOf(equipment));
    }

    public void triggerDestroyOnUnattachIfNeeded(GameData gameData, Permanent equipment,
                                                 UUID oldAttachedTo, UUID controllerId) {
        if (oldAttachedTo == null || controllerId == null) {
            return;
        }
        if (equipment.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, equipment)) {
            return;
        }
        Permanent snapshot = new Permanent(equipment);
        for (TriggeredAbility ability : MiscAbilityReader.printed(equipment.getCard(),
                MiscWatch.ON_EQUIPMENT_UNATTACHED)) {
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                    equipment.getCard(), equipment, controllerId, equipment.getCard().getName() + "'s ability",
                    ConditionContext.forPermanent(equipment, controllerId), entry -> {
                        entry.setNonTargeting(true);
                        entry.setTriggeringPermanentId(oldAttachedTo);
                        entry.setSourcePermanentSnapshot(snapshot);
                    }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
                log.info("Game {} - {} triggers on becoming unattached", gameData.id, equipment.getCard().getName());
            }
        }
    }
}
