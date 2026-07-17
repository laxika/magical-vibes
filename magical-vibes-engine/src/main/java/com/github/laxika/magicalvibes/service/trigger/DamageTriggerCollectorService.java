package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectSourceDamageToItsControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Trigger collectors for damage-related events (ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, ON_DEALT_DAMAGE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamageTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
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

        creatureControlService.applyControlEffect(gameData, sourceControllerId, match.permanent(),
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT, null, match.permanent().getCard().getName());

        log.info("Game {} - {} triggers, {} gains control of {}",
                gameData.id, match.permanent().getCard().getName(),
                gameData.playerIdToName.get(sourceControllerId), match.permanent().getCard().getName());
        return true;
    }

    // ── ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU ──────────────────────────

    @CollectsTrigger(value = DestroyDamageSourcePermanentEffect.class, slot = EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)
    private boolean handleDestroyDamageSourceOnDamage(TriggerMatchContext match,
            DestroyDamageSourcePermanentEffect destroyEffect, TriggerContext ctx) {
        TriggerContext.DamageToController dc = (TriggerContext.DamageToController) ctx;
        GameData gameData = match.gameData();

        Permanent currentSource = gameQueryService.findPermanentById(gameData, dc.sourcePermanentId());
        if (currentSource == null) return false;
        if (destroyEffect.filter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, currentSource, destroyEffect.filter())) {
            return false;
        }

        boolean destroyed = permanentRemovalService.tryDestroyPermanent(gameData, currentSource);
        if (destroyed) {
            String logEntry = match.permanent().getCard().getName() + " triggers - "
                    + currentSource.getCard().getName() + " is destroyed.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        }
        log.info("Game {} - {} triggers, destroying damage source {}",
                gameData.id, match.permanent().getCard().getName(), currentSource.getCard().getName());
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

    @CollectsTrigger(value = DealDamageToTargetOpponentOrPlaneswalkerEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageTargetOpponentOrPlaneswalker(TriggerMatchContext match,
            DealDamageToTargetOpponentOrPlaneswalkerEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        GameData gameData = match.gameData();
        UUID controllerId = gameQueryService.findPermanentController(gameData, dc.damagedCreature().getId());
        if (controllerId == null) return false;

        // Check if any planeswalkers are on the battlefield
        boolean hasPlaneswalkers = false;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf != null) {
                for (Permanent p : bf) {
                    if (p.getCard().hasType(CardType.PLANESWALKER)) {
                        hasPlaneswalkers = true;
                        break;
                    }
                }
            }
            if (hasPlaneswalkers) break;
        }

        if (!hasPlaneswalkers) {
            // In 2-player with no planeswalkers, auto-target the opponent
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    dc.damagedCreature().getCard(),
                    controllerId,
                    dc.damagedCreature().getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(trigger)),
                    null,
                    dc.damagedCreature().getId()
            );
            entry.setTargetId(opponentId);
            gameData.stack.add(entry);
        } else {
            // Planeswalkers present — need player choice between opponent and planeswalkers
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    dc.damagedCreature().getCard(), controllerId, new ArrayList<>(List.of(trigger)), false, null
            ));
        }

        String logEntry = dc.damagedCreature().getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} ON_DEALT_DAMAGE target-opponent-or-planeswalker trigger fires",
                gameData.id, dc.damagedCreature().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = DealDamageToAnyTargetEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageToAnyTarget(TriggerMatchContext match,
            DealDamageToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent damagedCreature = dc.damagedCreature();
        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return false;

        // "It deals that much damage to any target" (Spitemare): the damage amount snapshots
        // into xValue, and the controller chooses any target when the trigger is serviced.
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                damagedCreature.getCard(), controllerId, new ArrayList<>(List.of(trigger)),
                false, null, dc.damageDealt()));

        String logEntry = damagedCreature.getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} ON_DEALT_DAMAGE deal-damage-to-any-target trigger fires",
                gameData.id, damagedCreature.getCard().getName());
        return true;
    }

    // ── ON_ENCHANTED_CREATURE_DEALT_DAMAGE ─────────────────────────────

    @CollectsTrigger(value = EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect.class,
            slot = EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageToController(TriggerMatchContext match,
            EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent aura = match.permanent();
        Permanent enchantedCreature = dc.damagedCreature();

        UUID controllerId = gameQueryService.findPermanentController(gameData, enchantedCreature.getId());
        if (controllerId == null) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                match.controllerId(),
                aura.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                dc.damageDealt(),
                controllerId,
                aura.getId(),
                Map.of(),
                null,
                List.of(),
                List.of()
        );
        entry.setDamageSourceCard(enchantedCreature.getCard());
        gameData.stack.add(entry);

        String logEntry = aura.getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE trigger fires",
                gameData.id, aura.getCard().getName());
        return true;
    }

    // ── ON_CONTROLLER_DEALT_DAMAGE (Living Artifact) ───────────────────

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE)
    private boolean handleControllerDealtDamagePutCounters(TriggerMatchContext match,
            PutCountersOnSelfEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                match.controllerId(),
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                perm.getId());
        // Snapshot the damage dealt so the effect's EventValue amount ("put that many counters")
        // reads it back at resolution.
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        String logEntry = perm.getCard().getName() + "'s ability triggers.";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} ON_CONTROLLER_DEALT_DAMAGE trigger fires ({} damage)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    // ── ON_ANY_SOURCE_DEALS_DAMAGE (Justice) ───────────────────────────

    @CollectsTrigger(value = ReflectSourceDamageToItsControllerEffect.class, slot = EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE)
    private boolean handleReflectSourceDamage(TriggerMatchContext match,
            ReflectSourceDamageToItsControllerEffect trigger, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0) return false;
        if (!sourceHasColor(sd.sourceCard(), trigger.color())) return false;

        GameData gameData = match.gameData();
        UUID recipientId = sd.sourceControllerId();
        if (recipientId == null || !gameData.playerIds.contains(recipientId)) return false;

        Permanent watcher = match.permanent();
        // The watcher deals the summed damage to the red source's controller. Reuse the standard
        // TARGET_PLAYER damage effect with the recipient set as the (non-chosen) target.
        StackEntry se = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DealDamageToPlayersEffect(sd.totalDamage(), DamageRecipient.TARGET_PLAYER))),
                recipientId,
                watcher.getId());
        se.setNonTargeting(true);
        gameData.stack.add(se);

        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(watcher.getCard().getName()
                + "'s ability triggers — it deals " + sd.totalDamage() + " damage to "
                + gameData.playerIdToName.get(recipientId) + "."));
        log.info("Game {} - {} reflects {} damage to {}", gameData.id, watcher.getCard().getName(),
                sd.totalDamage(), gameData.playerIdToName.get(recipientId));
        return true;
    }

    private boolean sourceHasColor(Card card, CardColor color) {
        if (card == null || color == null) return false;
        if (card.getColor() == color) return true;
        return card.getColors().contains(color);
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
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} ON_DEALT_DAMAGE trigger fires", gameData.id, damagedCreature.getCard().getName());
    }
}
