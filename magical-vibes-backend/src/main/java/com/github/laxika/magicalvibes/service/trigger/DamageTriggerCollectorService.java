package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trigger collectors for damage-related events (ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, ON_DEALT_DAMAGE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamageTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final CreatureControlService creatureControlService;

    // ── ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU ───────────────────────────

    @CollectsTrigger(value = ReturnDamageSourcePermanentToHandEffect.class, slot = EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)
    private boolean handleBounceOnDamage(TriggerMatchContext match,
            ReturnDamageSourcePermanentToHandEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToController dc = (TriggerContext.DamageToController) ctx;
        var gameData = match.gameData();

        // Re-check source is still on the battlefield
        Permanent currentSource = gameQueryService.findPermanentById(gameData, dc.sourcePermanentId());
        if (currentSource == null) return false;

        // Bounce the source to its owner's hand
        if (permanentRemovalService.removePermanentToHand(gameData, currentSource)) {
            permanentRemovalService.removeOrphanedAuras(gameData);
            String logEntry = match.permanent().getCard().getName() + " triggers — "
                    + currentSource.getCard().getName() + " is returned to its owner's hand.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} triggers, bouncing {} to owner's hand",
                    gameData.id, match.permanent().getCard().getName(), currentSource.getCard().getName());
        }
        return true;
    }

    @CollectsTrigger(value = DamageSourceControllerGainsControlOfThisPermanentEffect.class, slot = EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)
    private boolean handleControlTheftOnDamage(TriggerMatchContext match,
            DamageSourceControllerGainsControlOfThisPermanentEffect controlEffect, TriggerContext ctx) {
        TriggerContext.DamageToController dc = (TriggerContext.DamageToController) ctx;
        var gameData = match.gameData();

        if (controlEffect.combatOnly() && !dc.isCombatDamage()) return false;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, dc.sourcePermanentId());
        if (sourcePermanent == null) return false;

        if (controlEffect.creatureOnly() && !gameQueryService.isCreature(gameData, sourcePermanent)) return false;

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, dc.sourcePermanentId());
        if (sourceControllerId == null || sourceControllerId.equals(dc.damagedPlayerId())) return false;

        creatureControlService.stealPermanent(gameData, sourceControllerId, match.permanent());
        gameData.permanentControlStolenCreatures.add(match.permanent().getId());

        log.info("Game {} - {} triggers, {} gains control of {}",
                gameData.id, match.permanent().getCard().getName(),
                gameData.playerIdToName.get(sourceControllerId), match.permanent().getCard().getName());
        return true;
    }

    // ── ON_DEALT_DAMAGE ────────────────────────────────────────────────

    @CollectsTrigger(value = DamageSourceControllerSacrificesPermanentsEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDamageSourceSacrifice(TriggerMatchContext match,
            DamageSourceControllerSacrificesPermanentsEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        CardEffect effectToAdd = trigger;
        if (dc.damageDealt() > 0 && dc.damageSourceControllerId() != null) {
            effectToAdd = new DamageSourceControllerSacrificesPermanentsEffect(dc.damageDealt(), dc.damageSourceControllerId());
        }
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effectToAdd);
        return true;
    }

    @CollectsTrigger(value = DamageSourceControllerGetsPoisonCounterEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDamageSourcePoisonCounter(TriggerMatchContext match,
            DamageSourceControllerGetsPoisonCounterEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        CardEffect effectToAdd = trigger;
        if (dc.damageSourceControllerId() != null) {
            effectToAdd = new DamageSourceControllerGetsPoisonCounterEffect(dc.damageSourceControllerId());
        }
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effectToAdd);
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effect);
        return true;
    }

    private void addDealtDamageEntry(com.github.laxika.magicalvibes.model.GameData gameData,
            Permanent damagedCreature, CardEffect effect) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                damagedCreature.getCard(),
                controllerId,
                damagedCreature.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                damagedCreature.getId()
        ));
        String logEntry = damagedCreature.getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} ON_DEALT_DAMAGE trigger fires", gameData.id, damagedCreature.getCard().getName());
    }
}
