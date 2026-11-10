package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.MiscAbilityReader;
import com.github.laxika.magicalvibes.service.trigger.MiscWatch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Puts the "whenever this Equipment becomes unattached from a permanent" abilities of an Equipment that became
 * unattached on the stack ({@code Triggers.becomesUnattachedFromPermanent()}: Captain's Hook, Grafted Wargear,
 * Grafted Exoskeleton, Stitcher's Graft). Each ability is one object of all its effects (CR 603.3) and records the
 * permanent the Equipment was attached to as its triggering permanent, "that permanent", whatever its effects are, so
 * the permanent stays on the battlefield until the ability resolves.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class UnattachTriggerSupport {

    private final GameLogService gameLogService;

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
        for (List<CardEffect> effects : MiscAbilityReader.printedEffectsByAbility(equipment.getCard(),
                MiscWatch.ON_EQUIPMENT_UNATTACHED)) {
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    equipment.getCard(),
                    controllerId,
                    equipment.getCard().getName() + "'s ability",
                    new ArrayList<>(effects),
                    null,
                    equipment.getId()
            );
            trigger.setNonTargeting(true);
            trigger.setTriggeringPermanentId(oldAttachedTo);
            gameData.enqueueTrigger(trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} triggers on becoming unattached", gameData.id, equipment.getCard().getName());
        }
    }
}
