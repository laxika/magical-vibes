package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Materializes triggered abilities for Equipment that becomes unattached.
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
        if (oldAttachedTo == null || !hasDestroyOnUnattach(equipment)) {
            return;
        }

        if (controllerId == null) {
            return;
        }

        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                equipment.getCard(),
                controllerId,
                equipment.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING))),
                null,
                equipment.getId()
        );
        trigger.setNonTargeting(true);
        trigger.setTriggeringPermanentId(oldAttachedTo);
        gameData.enqueueTrigger(trigger);
        gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
        log.info("Game {} - {} triggers on becoming unattached", gameData.id, equipment.getCard().getName());
    }

    private boolean hasDestroyOnUnattach(Permanent equipment) {
        for (CardEffect effect : equipment.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof DestroyOnUnattachEffect) {
                return true;
            }
        }
        return false;
    }
}
