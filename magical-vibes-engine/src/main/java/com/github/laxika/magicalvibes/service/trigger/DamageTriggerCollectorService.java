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
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerMillsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.effect.ReflectDamageToChosenColorCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectSourceDamageToItsControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final GameLogService gameLogService;
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
            gameLogService.append(gameData, GameLog.cardTextCard(match.permanent().getCard(),
                    " triggers — ", currentSource.getCard(), " is returned to its owner's hand."));
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
            gameLogService.append(gameData, GameLog.cardTextCard(match.permanent().getCard(),
                    " triggers - ", currentSource.getCard(), " is destroyed."));
        }
        log.info("Game {} - {} triggers, destroying damage source {}",
                gameData.id, match.permanent().getCard().getName(), currentSource.getCard().getName());
        return true;
    }

    // ── ON_DEALT_DAMAGE ────────────────────────────────────────────────

    @CollectsTriggers({
        @CollectsTrigger(value = DamageSourceControllerSacrificesPermanentsEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE),
        @CollectsTrigger(value = DamageSourceControllerGetsPoisonCounterEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE),
        @CollectsTrigger(value = DamageSourceControllerMillsEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    })
    private boolean handleDamageSourceControllerAware(TriggerMatchContext match,
            DamageSourceControllerAwareEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        CardEffect effectToAdd = trigger.bindDamageSourceController(dc.damageSourceControllerId(), dc.damageDealt());
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effectToAdd, dc.damageDealt());
        return true;
    }

    @CollectsTrigger(value = DealDamageToTargetPlayerOrPlaneswalkerEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageTargetPlayerOrPlaneswalker(TriggerMatchContext match,
            DealDamageToTargetPlayerOrPlaneswalkerEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        GameData gameData = match.gameData();
        if (trigger.playerRelation() != PlayerRelation.OPPONENT) {
            // Only the opponent-only wording has a single implied player to auto-target; the plain
            // "target player" form queues as an ordinary trigger, exactly as the default collector
            // handled it before this class absorbed its opponent-only sibling.
            addDealtDamageEntry(gameData, dc.damagedCreature(), trigger, dc.damageDealt());
            return true;
        }
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

        gameLogService.append(gameData, GameLog.abilityTriggers(dc.damagedCreature().getCard()));
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

        gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
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

        gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE trigger fires",
                gameData.id, aura.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = EnchantedCreatureControllerLosesLifeEffect.class,
            slot = EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageControllerLosesLife(TriggerMatchContext match,
            EnchantedCreatureControllerLosesLifeEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent aura = match.permanent();

        UUID controllerId = gameQueryService.findPermanentController(gameData, dc.damagedCreature().getId());
        if (controllerId == null) return false;

        // Ragged Veins: the card-def amount is a placeholder; bake in the damage just dealt.
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                match.controllerId(),
                aura.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new EnchantedCreatureControllerLosesLifeEffect(dc.damageDealt(), controllerId))),
                null,
                aura.getId()
        ));
        gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE life-loss trigger fires",
                gameData.id, aura.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = DestroyReferencedPermanentEffect.class,
            slot = EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageDestroy(TriggerMatchContext match,
            DestroyReferencedPermanentEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent aura = match.permanent();

        // Mortal Wound: bake the Aura's permanent id so resolution re-derives the enchanted creature.
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                match.controllerId(),
                aura.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                aura.getId()
        ));
        gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE destroy trigger fires",
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

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_CONTROLLER_DEALT_DAMAGE trigger fires ({} damage)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    @CollectsTrigger(value = RemoveCounterFromSourceEffect.class, slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE)
    private boolean handleControllerDealtDamageRemoveCounter(TriggerMatchContext match,
            RemoveCounterFromSourceEffect effect, TriggerContext ctx) {
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
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_CONTROLLER_DEALT_DAMAGE trigger fires, removing a counter",
                gameData.id, perm.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)
    private boolean handleAllySourceDealtDamageToOpponentPutCounters(TriggerMatchContext match,
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
        // Snapshot the damage dealt so the effect's EventValue amount ("put that many theft
        // counters") reads it back at resolution.
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT trigger fires ({} damage)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    // ── ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT (Retaliator Griffin) ────

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)
    private boolean handleControllerDealtDamageByOpponentMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                match.controllerId(),
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(may)),
                null,
                perm.getId());
        // Snapshot the damage dealt so the wrapped effect's EventValue amount ("put that many
        // +1/+1 counters") reads it back at resolution, after the "you may" is accepted.
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT trigger fires ({} damage)",
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

        gameLogService.append(gameData, GameLog.cardThen(watcher.getCard(),
                "'s ability triggers — it deals " + sd.totalDamage() + " damage to "
                        + gameData.playerIdToName.get(recipientId) + "."));
        log.info("Game {} - {} reflects {} damage to {}", gameData.id, watcher.getCard().getName(),
                sd.totalDamage(), gameData.playerIdToName.get(recipientId));
        return true;
    }

    // ── ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT (Mangara's Equity) ──

    @CollectsTrigger(value = ReflectDamageToChosenColorCreatureEffect.class,
            slot = EffectSlot.ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)
    private boolean handleChosenColorCreatureDamageReflection(TriggerMatchContext match,
            ReflectDamageToChosenColorCreatureEffect trigger, TriggerContext ctx) {
        TriggerContext.CreatureDamageToYouOrYourPermanent dc =
                (TriggerContext.CreatureDamageToYouOrYourPermanent) ctx;
        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();

        CardColor chosenColor = watcher.getChosenColor();
        if (chosenColor == null) return false;

        Permanent damageSource = dc.damageSource();
        if (!gameQueryService.isCreature(gameData, damageSource)) return false;
        if (!gameQueryService.getEffectiveColors(gameData, damageSource).contains(chosenColor)) return false;

        // "to you or a [filtered] permanent you control" — damage to the controller always counts;
        // damage to one of their permanents only when it matches the filter.
        if (dc.damagedPermanent() != null && trigger.damagedPermanentFilter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, dc.damagedPermanent(),
                        trigger.damagedPermanentFilter())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new DealDamageToTargetCreatureEffect(dc.damage()))),
                damageSource.getId(),
                watcher.getId());
        entry.setNonTargeting(true);
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.cardTextCard(watcher.getCard(),
                " triggers — it deals " + dc.damage() + " damage to ", damageSource.getCard(), "."));
        log.info("Game {} - {} reflects {} damage to {}", gameData.id, watcher.getCard().getName(),
                dc.damage(), damageSource.getCard().getName());
        return true;
    }

    // ── ON_SELF_DEALS_DAMAGE (El-Hajjâj) ───────────────────────────────

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_DEALS_DAMAGE)
    private boolean handleSelfDealsDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        GameData gameData = match.gameData();
        Card sourceCard = sd.sourceCard();
        // The source may have died dealing the damage; keep its last-known permanent id when present.
        UUID sourcePermanentId = match.permanent() != null ? match.permanent().getId() : null;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                sourcePermanentId);
        // Snapshot the total damage dealt so an EventValue amount ("you gain that much life") reads it.
        entry.setEventValue(sd.totalDamage());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} ON_SELF_DEALS_DAMAGE trigger fires ({} damage)",
                gameData.id, sourceCard.getName(), sd.totalDamage());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE)
    private boolean handleSelfDealsCombatDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        Card sourceCard = sd.sourceCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sd.sourceControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                sd.sourcePermanentId());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} ON_SELF_DEALS_COMBAT_DAMAGE trigger fires ({} damage)",
                match.gameData().id, sourceCard.getName(), sd.totalDamage());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE)
    private boolean handleAllyCreatureDealsCombatDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;

        Card watcherCard = match.permanent().getCard();
        // The watcher, not the damage dealer, is the trigger's source permanent so a self-referencing
        // effect (Five-Alarm Fire's blaze counter) lands on the watcher.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcherCard,
                match.controllerId(),
                watcherCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setEventValue(sd.totalDamage());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcherCard));
        log.info("Game {} - {} ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE trigger fires ({} damage by {})",
                match.gameData().id, watcherCard.getName(), sd.totalDamage(), sd.sourceCard().getName());
        return true;
    }

    private boolean sourceHasColor(Card card, CardColor color) {
        if (card == null || color == null) return false;
        if (card.getColor() == color) return true;
        return card.getColors().contains(color);
    }

    /**
     * "Whenever this creature is dealt damage, you may destroy target nonland permanent"
     * (High Priest of Penance). CR 603.3d: a targeted "may" trigger chooses its target as the
     * ability goes on the stack, while the "you may" is answered on resolution — so the target
     * choice is queued here, honouring the card's own target filter. Non-targeting mays keep the
     * plain trigger entry, whose targetId is the damaged permanent itself.
     */
    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageMayEffect(TriggerMatchContext match, MayEffect may, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (!may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            return handleDealtDamageDefault(match, may, ctx);
        }

        GameData gameData = match.gameData();
        Permanent damagedCreature = dc.damagedCreature();
        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return false;

        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                damagedCreature.getCard(), controllerId, new ArrayList<>(List.of(may)),
                false, damagedCreature.getCard().getTargetFilter(), 0, damagedCreature.getId()));

        gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
        log.info("Game {} - {} ON_DEALT_DAMAGE targeted-may trigger fires",
                gameData.id, damagedCreature.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effect, dc.damageDealt());
        return true;
    }

    /**
     * Queues a plain ON_DEALT_DAMAGE triggered ability. The damage dealt is snapshotted onto the
     * entry's eventValue so "it deals that much damage" effects can read it with an
     * {@code EventValue} amount (Stuffy Doll).
     */
    private void addDealtDamageEntry(com.github.laxika.magicalvibes.model.GameData gameData,
            Permanent damagedCreature, CardEffect effect, int damageDealt) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        StackEntry triggerEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                damagedCreature.getCard(),
                controllerId,
                damagedCreature.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                damagedCreature.getId()
        );
        triggerEntry.setEventValue(damageDealt);
        gameData.stack.add(triggerEntry);
        gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
        log.info("Game {} - {} ON_DEALT_DAMAGE trigger fires", gameData.id, damagedCreature.getCard().getName());
    }
}
