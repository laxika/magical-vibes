package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeTriggeringPermanentEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Materializes triggered abilities for Equipment that becomes unattached: the destroy-on-unattach
 * ability and the sacrifice-on-unattach ability (Grafted Wargear, Stitcher's Graft). Both go on the
 * stack as triggers that name the previously-attached permanent, so the permanent stays on the
 * battlefield until the trigger resolves.
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

        if (hasStaticEffect(equipment, DestroyOnUnattachEffect.class)) {
            enqueueUnattachTrigger(gameData, equipment, oldAttachedTo, controllerId,
                    new DestroyReferencedPermanentEffect(PermanentReference.TRIGGERING));
        }
        if (hasStaticEffect(equipment, SacrificeOnUnattachEffect.class)) {
            enqueueUnattachTrigger(gameData, equipment, oldAttachedTo, controllerId,
                    new SacrificeTriggeringPermanentEffect());
        }
    }

    private void enqueueUnattachTrigger(GameData gameData, Permanent equipment, UUID oldAttachedTo,
                                        UUID controllerId, CardEffect effect) {
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                equipment.getCard(),
                controllerId,
                equipment.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                equipment.getId()
        );
        trigger.setNonTargeting(true);
        trigger.setTriggeringPermanentId(oldAttachedTo);
        gameData.enqueueTrigger(trigger);
        gameLogService.append(gameData, GameLog.abilityTriggers(equipment.getCard()));
        log.info("Game {} - {} triggers on becoming unattached", gameData.id, equipment.getCard().getName());
    }

    private boolean hasStaticEffect(Permanent equipment, Class<? extends CardEffect> effectType) {
        for (CardEffect effect : equipment.getCard().getStaticEffects()) {
            if (effectType.isInstance(effect)) {
                return true;
            }
        }
        return false;
    }
}
