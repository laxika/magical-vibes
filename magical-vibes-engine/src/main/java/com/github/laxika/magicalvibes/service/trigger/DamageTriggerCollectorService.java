package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfThisPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGainsControlOfDamagedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerExilesRandomHandCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerAwareEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerGetsPoisonCounterEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerMillsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToDamageSourceCreatureOrSpellControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.effect.ReflectDamageToChosenColorCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureControllerLosesLifeEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyDamageSourcePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDamageSourcePermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDamagedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.RemoveCounterFromSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ReflectSourceDamageToItsControllerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnDamageSourcePermanentToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
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
 * Trigger collectors for damage-related events (ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE,
 * ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU, ON_DEALT_DAMAGE).
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
    private final ConditionEvaluationService conditionEvaluationService;

    @CollectsTrigger(value = TriggeringPermanentConditionalEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER)
    private boolean handleAllyCreatureDealsDamageToPlaneswalker(TriggerMatchContext match,
            TriggeringPermanentConditionalEffect trigger, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToPlaneswalker damageContext =
                (TriggerContext.CreatureDealsDamageToPlaneswalker) ctx;
        Permanent watcher = match.permanent();
        if (watcher == null || damageContext.damageSource() == null || damageContext.damage() <= 0
                || !gameQueryService.isCreature(match.gameData(), damageContext.damageSource())) return false;
        if (trigger.predicate() != null
                && !predicateEvaluationService.matchesPermanentPredicate(
                damageContext.damageSource(), trigger.predicate(), FilterContext.of(match.gameData())
                        .withSourceCardId(watcher.getCard().getId())
                        .withSourceControllerId(match.controllerId())
                        .withSourcePermanentId(watcher.getId())
                        .withSourcePermanentSnapshot(watcher))) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger.wrapped())),
                damageContext.damagedPlaneswalkerId(),
                watcher.getId());
        entry.setTriggeringPermanentId(damageContext.damageSource().getId());
        entry.setNonTargeting(true);
        if (damageContext.deferredTriggers() == null) {
            match.gameData().enqueueTrigger(entry);
        } else {
            damageContext.deferredTriggers().add(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers after creature damage to a planeswalker",
                match.gameData().id, watcher.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE)
    private boolean handleAllyCreatureDealsCombatDamageToCreatureMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToCreature dc = (TriggerContext.CreatureDealsDamageToCreature) ctx;
        if (!dc.combatDamage() || dc.damagedCreatureId() == null || match.permanent() == null) return false;

        GameData gameData = match.gameData();
        gameData.queueMayAbility(match.permanent().getCard(), match.controllerId(), may,
                null, match.permanent().getId());

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after an ally creature dealt combat damage to a creature",
                gameData.id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = DrawCardEffect.class,
            slot = EffectSlot.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE)
    private boolean handleAllySourceDealsNoncombatDamageToCreatureDraw(TriggerMatchContext match,
            DrawCardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsNoncombatDamageToCreature damageContext =
                (TriggerContext.SourceDealsNoncombatDamageToCreature) ctx;
        Permanent damagedCreature = damageContext.damagedCreature();
        GameData gameData = match.gameData();
        if (match.permanent() == null || damagedCreature == null
                || !gameQueryService.isCreature(gameData, damagedCreature)
                || damageContext.damageDealt() != gameQueryService.getEffectiveToughness(gameData, damagedCreature)) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after a source dealt noncombat damage equal to a creature's toughness",
                gameData.id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSourceEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE)
    private boolean handleAllyDealtDamageToCreaturePutCounters(TriggerMatchContext match,
            PutCountersOnSourceEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToCreature dc = (TriggerContext.CreatureDealsDamageToCreature) ctx;
        if (dc.damageSource() == null || dc.damageDealt() <= 0) return false;

        Permanent source = dc.damageSource();
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers to put a counter on itself", gameData.id, source.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = CreateTokenEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE)
    private boolean handleSelfDealsDamageToCreatureCreateToken(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToCreature dc = (TriggerContext.CreatureDealsDamageToCreature) ctx;
        Permanent source = match.permanent();
        if (source == null || dc.damageSource() == null || dc.damageDealt() <= 0
                || !source.getId().equals(dc.damageSource().getId())) {
            return false;
        }

        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        log.info("Game {} - {} triggers to create a token after dealing damage to a creature",
                gameData.id, source.getCard().getName());
        return true;
    }

    // ── ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU ───────────────────────────

    @CollectsTrigger(value = ExileDamagedCreatureEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE)
    private boolean handleExileDamagedCreature(TriggerMatchContext match,
            ExileDamagedCreatureEffect exileEffect, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToCreature dc = (TriggerContext.CreatureDealsDamageToCreature) ctx;
        Permanent watcher = match.permanent();
        if (watcher == null) return false;
        Permanent triggerSource = dc.damageSource();
        if (exileEffect.equipmentScoped()) {
            if (!watcher.isAttached() || !watcher.getAttachedTo().equals(dc.damageSource().getId())) return false;
            triggerSource = watcher;
        } else if (!watcher.getId().equals(dc.damageSource().getId())) {
            return false;
        }
        if (dc.damagedCreatureId() == null) return false;

        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                triggerSource.getCard(),
                match.controllerId(),
                triggerSource.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(new ExileTargetPermanentEffect())),
                dc.damagedCreatureId(),
                triggerSource.getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers, exiling the creature it damaged",
                gameData.id, watcher.getCard().getName());
        return true;
    }

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

    @CollectsTrigger(value = PutCounterOnReferencedPermanentEffect.class,
            slot = EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)
    private boolean handleCreatureDealsDamageToYouPutCounter(TriggerMatchContext match,
            PutCounterOnReferencedPermanentEffect effect, TriggerContext ctx) {
        if (effect.reference() != PermanentReference.TRIGGERING) return false;

        TriggerContext.DamageToController dc = (TriggerContext.DamageToController) ctx;
        GameData gameData = match.gameData();
        Permanent damageSource = gameQueryService.findPermanentById(gameData, dc.sourcePermanentId());
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return false;

        Permanent watcher = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                watcher.getId());
        entry.setTriggeringPermanentId(damageSource.getId());
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers to put a counter on the creature that dealt damage",
                gameData.id, watcher.getCard().getName());
        return true;
    }

    @CollectsTrigger(value = ExileDamageSourcePermanentUntilSourceLeavesEffect.class,
            slot = EffectSlot.ON_ANY_PERMANENT_DEALS_DAMAGE_TO_YOU)
    private boolean handleExileDamageSourceOnDamage(TriggerMatchContext match,
            ExileDamageSourcePermanentUntilSourceLeavesEffect exileEffect, TriggerContext ctx) {
        TriggerContext.DamageToController dc = (TriggerContext.DamageToController) ctx;
        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();

        if (exileEffect.combatOnly() && !dc.isCombatDamage()) return false;

        Permanent damageSource = gameQueryService.findPermanentById(gameData, dc.sourcePermanentId());
        if (damageSource == null) return false;
        if (exileEffect.filter() != null
                && !predicateEvaluationService.matchesPermanentPredicate(gameData, damageSource, exileEffect.filter())) {
            return false;
        }
        // CR 603.4 — the intervening-"if" is checked as the ability would trigger, and the wrapper
        // below makes the resolution-time re-check happen too.
        if (exileEffect.intervening() != null
                && !conditionEvaluationService.isMet(gameData, exileEffect.intervening(),
                        ConditionContext.forPermanent(watcher, match.controllerId()))) {
            return false;
        }

        CardEffect queued = new ExileTargetPermanentUntilSourceLeavesEffect();
        if (exileEffect.intervening() != null) {
            queued = new ConditionalEffect(exileEffect.intervening(), queued);
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(queued)),
                damageSource.getId(),
                watcher.getId());
        // "exile that creature" — the damaging creature is not a chosen target.
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers, exiling damage source {} until it leaves",
                gameData.id, watcher.getCard().getName(), damageSource.getCard().getName());
        return true;
    }

    // ── ON_DEALT_DAMAGE ────────────────────────────────────────────────

    @CollectsTrigger(value = DealDamageToDamageSourceCreatureOrSpellControllerEffect.class,
            slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDamageSourceCreatureOrSpellController(
            TriggerMatchContext match, DealDamageToDamageSourceCreatureOrSpellControllerEffect trigger,
            TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        CardEffect effectToAdd = trigger.bindDamageSource(
                dc.sourceCard(), dc.sourcePermanentId(), dc.damageSourceControllerId(), dc.damageDealt());
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effectToAdd, dc.damageDealt());
        return true;
    }

    @CollectsTriggers({
        @CollectsTrigger(value = DamageSourceControllerSacrificesPermanentsEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE),
        @CollectsTrigger(value = DamageSourceControllerGetsPoisonCounterEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE),
        @CollectsTrigger(value = DamageSourceControllerMillsEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE),
        @CollectsTrigger(value = DamageSourceControllerExilesRandomHandCardEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE),
        @CollectsTrigger(value = DamageSourceControllerGainsControlOfDamagedPermanentEffect.class,
                slot = EffectSlot.ON_DEALT_DAMAGE)
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
            TargetFilter targetFilter = targetFilterFor(dc.damagedCreature(), trigger);
            boolean playerTargetOnly = targetFilter instanceof PlayerPredicateTargetFilter;
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    dc.damagedCreature().getCard(),
                    gameQueryService.findPermanentController(gameData, dc.damagedCreature().getId()),
                    new ArrayList<>(List.of(trigger)),
                    playerTargetOnly,
                    targetFilter,
                    dc.damageDealt()));
            gameLogService.append(gameData, GameLog.abilityTriggers(dc.damagedCreature().getCard()));
            log.info("Game {} - {} ON_DEALT_DAMAGE target trigger fires",
                    gameData.id, dc.damagedCreature().getCard().getName());
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
                    dc.damageDealt(),
                    dc.damagedCreature().getId()
            );
            entry.setTargetId(opponentId);
            gameData.stack.add(entry);
        } else {
            // Planeswalkers present — need player choice between opponent and planeswalkers
            TargetFilter targetFilter = targetFilterFor(dc.damagedCreature(), trigger);
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    dc.damagedCreature().getCard(), controllerId, new ArrayList<>(List.of(trigger)),
                    false, targetFilter, dc.damageDealt()
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

    @CollectsTrigger(value = DealDamageToAnyTargetEffect.class,
            slot = EffectSlot.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER)
    private boolean handleCountersPlaced(TriggerMatchContext match,
            DealDamageToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.CountersPlaced countersPlaced = (TriggerContext.CountersPlaced) ctx;
        if (countersPlaced.amount() <= 0) return false;

        Card sourceCard = match.permanent().getCard();
        DealDamageToAnyTargetEffect damage = new DealDamageToAnyTargetEffect(countersPlaced.amount());
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(damage)), false,
                sourceCard.getTargetFilter(), 0, match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} counter-placement trigger fires for {} damage",
                match.gameData().id, sourceCard.getName(), countersPlaced.amount());
        return true;
    }

    @CollectsTrigger(value = EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect.class,
            slot = EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE)
    private boolean handleAnyCreatureDealtDamageToController(TriggerMatchContext match,
            EnchantedCreatureDealsDamageEqualToDealtDamageToControllerEffect trigger,
            TriggerContext ctx) {
        TriggerContext.AnyCreatureDealtDamage dc = (TriggerContext.AnyCreatureDealtDamage) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger)),
                dc.damageDealt(),
                dc.damagedCreatureControllerId(),
                match.permanent().getId(),
                Map.of(),
                null,
                List.of(),
                List.of()
        );
        entry.setNonTargeting(true);
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers for damage dealt to a creature", gameData.id,
                match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = TriggeringPermanentConditionalEffect.class,
            slot = EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE)
    private boolean handleAnyCreatureDealtDamagePermanentConditional(TriggerMatchContext match,
            TriggeringPermanentConditionalEffect conditional, TriggerContext ctx) {
        TriggerContext.AnyCreatureDealtDamage dc = (TriggerContext.AnyCreatureDealtDamage) ctx;
        if (dc.damageDealt() <= 0) return false;

        FilterContext filterContext = FilterContext.of(match.gameData())
                .withSourceCardId(match.permanent().getCard().getId())
                .withSourceControllerId(match.controllerId())
                .withSourcePermanentSnapshot(match.permanent());
        if (!predicateEvaluationService.matchesPermanentPredicate(dc.damagedCreature(), conditional.predicate(),
                filterContext)) {
            return false;
        }

        return enqueueAnyCreatureDealtDamage(match, conditional.wrapped(), dc);
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ANY_CREATURE_DEALT_DAMAGE)
    private boolean handleAnyCreatureDealtDamageDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyCreatureDealtDamage dc = (TriggerContext.AnyCreatureDealtDamage) ctx;
        if (dc.damageDealt() <= 0) return false;

        return enqueueAnyCreatureDealtDamage(match, effect, dc);
    }

    private boolean enqueueAnyCreatureDealtDamage(TriggerMatchContext match, CardEffect effect,
                                                   TriggerContext.AnyCreatureDealtDamage dc) {
        GameData gameData = match.gameData();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                match.permanent().getId()
        );
        entry.setTargetId(dc.damagedCreature().getId());
        entry.setEventValue(dc.damageDealt());
        entry.setNonTargeting(true);
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers for damage dealt to a creature", gameData.id,
                match.permanent().getCard().getName());
        return true;
    }

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

    @CollectsTrigger(value = GainLifeEffect.class, slot = EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageGainLife(TriggerMatchContext match, GainLifeEffect effect,
                                                                TriggerContext ctx) {
        return enqueueEnchantedCreatureDealtDamageTrigger(match, effect, ctx);
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageDefault(TriggerMatchContext match, CardEffect effect,
                                                               TriggerContext ctx) {
        return enqueueEnchantedCreatureDealtDamageTrigger(match, effect, ctx);
    }

    private boolean enqueueEnchantedCreatureDealtDamageTrigger(TriggerMatchContext match, CardEffect effect,
                                                                TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        Permanent aura = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                match.controllerId(),
                aura.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                aura.getId());
        entry.setEventValue(dc.damageDealt());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE trigger fires ({} damage)",
                match.gameData().id, aura.getCard().getName(), dc.damageDealt());
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

    @CollectsTrigger(value = CreateTokenEffect.class,
            slot = EffectSlot.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageCreateTokens(TriggerMatchContext match,
            CreateTokenEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent aura = match.permanent();
        UUID enchantedCreatureControllerId = gameQueryService.findPermanentController(
                gameData, dc.damagedCreature().getId());
        if (enchantedCreatureControllerId == null) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                enchantedCreatureControllerId,
                aura.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                aura.getId()
        );
        entry.setEventValue(dc.damageDealt());
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE token trigger fires",
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

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE)
    private boolean handleControllerDealtDamageMay(TriggerMatchContext match,
            MayEffect effect, TriggerContext ctx) {
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
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_CONTROLLER_DEALT_DAMAGE may trigger fires ({} damage)",
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
        return queueAllySourceDealtDamageToOpponentTrigger(match, effect, ctx);
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)
    private boolean handleAllySourceDealtDamageToOpponentMay(TriggerMatchContext match,
            MayEffect effect, TriggerContext ctx) {
        return queueAllySourceDealtDamageToOpponentTrigger(match, effect, ctx);
    }

    @CollectsTrigger(value = TriggeringPermanentConditionalEffect.class,
            slot = EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)
    private boolean handleAllySourceDealtDamageToOpponentConditional(TriggerMatchContext match,
            TriggeringPermanentConditionalEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount damageContext = (TriggerContext.DamageToControllerAmount) ctx;
        Permanent source = damageContext.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(match.gameData(), damageContext.sourcePermanentId());
        Permanent watcher = match.permanent();
        if (source == null || watcher == null || !gameQueryService.isCreature(match.gameData(), source)) {
            return false;
        }
        if (trigger.predicate() != null
                && !predicateEvaluationService.matchesPermanentPredicate(source, trigger.predicate(),
                FilterContext.of(match.gameData())
                        .withSourceCardId(watcher.getCard().getId())
                        .withSourceControllerId(match.controllerId())
                        .withSourcePermanentId(watcher.getId())
                        .withSourcePermanentSnapshot(watcher))) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(trigger.wrapped())),
                null,
                watcher.getId());
        entry.setTriggeringPermanentId(source.getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers after a creature dealt damage to an opponent",
                match.gameData().id, watcher.getCard().getName());
        return true;
    }

    private boolean queueAllySourceDealtDamageToOpponentTrigger(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();
        CardEffect counterEffect = effect instanceof MayEffect may ? may.wrapped() : effect;
        if (counterEffect instanceof PutCountersOnSelfEffect putCounters
                && putCounters.excludeDamageSource()
                && perm.getId().equals(dc.sourcePermanentId())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                match.controllerId(),
                perm.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                perm.getId());
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT trigger fires ({} damage)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class,
            slot = EffectSlot.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)
    private boolean handleSelfDealsDamageToOpponent(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        Permanent source = match.permanent();
        if (source == null || dc.sourcePermanentId() == null
                || !source.getId().equals(dc.sourcePermanentId())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId());
        entry.setEventValue(dc.amount());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(source.getCard()));
        return true;
    }

    @CollectsTrigger(value = DealDamageToPlayersEffect.class,
            slot = EffectSlot.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE)
    private boolean handleSelfDealsDamageToCreature(TriggerMatchContext match,
            DealDamageToPlayersEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToCreature dc = (TriggerContext.CreatureDealsDamageToCreature) ctx;
        Permanent source = match.permanent();
        if (source == null || !source.getId().equals(dc.damageSource().getId())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                source.getCard(),
                match.controllerId(),
                source.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                source.getId());
        entry.setEventValue(dc.damageDealt());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(source.getCard()));
        return true;
    }

    @CollectsTrigger(value = PutCountersOnSelfEffect.class, slot = EffectSlot.ON_OPPONENT_DEALT_DAMAGE)
    private boolean handleOpponentDealtDamagePutCounters(TriggerMatchContext match,
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
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_OPPONENT_DEALT_DAMAGE trigger fires ({} damage)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    // ── ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT (Retaliator Griffin) ────

    @CollectsTrigger(value = SacrificePermanentsEffect.class,
            slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)
    private boolean handleControllerDealtDamageByOpponentSacrifice(TriggerMatchContext match,
            SacrificePermanentsEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        UUID sourceControllerId = dc.sourceControllerId();
        if (effect.recipient() != SacrificeRecipient.TARGET_PLAYER
                || sourceControllerId == null
                || !match.gameData().playerIds.contains(sourceControllerId)) {
            return false;
        }

        Permanent watcher = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                sourceControllerId,
                watcher.getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers for source controller {} to sacrifice a permanent",
                match.gameData().id, watcher.getCard().getName(),
                match.gameData().playerIdToName.get(sourceControllerId));
        return true;
    }

    @CollectsTrigger(value = ConditionalEffect.class, slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)
    private boolean handleControllerDealtDamageByOpponentConditional(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        if (conditional.interveningIf()
                && !conditionEvaluationService.isInterveningIfMet(match.gameData(), conditional,
                        match.permanent(), match.controllerId())) {
            return false;
        }
        return queueControllerDealtDamageByOpponentTrigger(match, conditional, ctx);
    }

    @CollectsTrigger(value = MayEffect.class, slot = EffectSlot.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)
    private boolean handleControllerDealtDamageByOpponentMay(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        return queueControllerDealtDamageByOpponentTrigger(match, may, ctx);
    }

    private boolean queueControllerDealtDamageByOpponentTrigger(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
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
        // Preserve the damage amount for damage-trigger effects that read it at resolution.
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT trigger fires ({} damage)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    @CollectsTrigger(value = MayEffect.class,
            slot = EffectSlot.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)
    private boolean handleOpponentSourceDamageToYouOrYourPermanent(TriggerMatchContext match,
            MayEffect may, TriggerContext ctx) {
        TriggerContext.SourceDamageToYouOrYourPermanent damage =
                (TriggerContext.SourceDamageToYouOrYourPermanent) ctx;
        if (match.permanent() == null || damage.sourceControllerId() == null
                || damage.sourceControllerId().equals(match.controllerId())) {
            return false;
        }

        MayEffect boundMay = new MayEffect(
                new DealDamageToDamageSourceControllerEffect(
                        1, damage.sourcePermanentId(), damage.sourceControllerId()),
                may.prompt(), may.elseEffect(), may.choicePlayer());
        match.gameData().queueMayAbility(match.permanent().getCard(), match.controllerId(), boundMay,
                null, match.permanent().getId());
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after an opponent-controlled source dealt damage to its controller or permanent",
                match.gameData().id, match.permanent().getCard().getName());
        return true;
    }

    @CollectsTrigger(value = LoseLifeUnlessPaysEffect.class,
            slot = EffectSlot.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)
    private boolean handleOpponentSourceDamageLifeUnlessPays(TriggerMatchContext match,
            LoseLifeUnlessPaysEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDamageToYouOrYourPermanent damage =
                (TriggerContext.SourceDamageToYouOrYourPermanent) ctx;
        if (match.permanent() == null || damage.sourceControllerId() == null
                || damage.sourceControllerId().equals(match.controllerId())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                damage.sourceControllerId(),
                match.permanent().getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(match.permanent().getCard()));
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

    @CollectsTrigger(value = GainLifeEffect.class, slot = EffectSlot.ON_ANY_SOURCE_DEALS_DAMAGE)
    private boolean handleGainLifeOnNoncreatureSourceDamage(TriggerMatchContext match,
            GainLifeEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0 || !match.controllerId().equals(sd.sourceControllerId())) return false;
        boolean creatureSource = sd.sourceCard().hasType(CardType.CREATURE);
        if (sd.sourcePermanentId() != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(match.gameData(), sd.sourcePermanentId());
            if (sourcePermanent != null) {
                creatureSource = gameQueryService.isCreature(match.gameData(), sourcePermanent);
            }
        }
        if (creatureSource) return false;

        Permanent watcher = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                watcher.getId());
        entry.setEventValue(sd.totalDamage());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers for {} damage from a noncreature source",
                match.gameData().id, watcher.getCard().getName(), sd.totalDamage());
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

        if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()) {
            ConditionContext conditionContext = match.permanent() != null
                    ? ConditionContext.forPermanent(match.permanent(), match.controllerId())
                    : ConditionContext.forCard(sourceCard, match.controllerId());
            if (!conditionEvaluationService.isMet(gameData, conditional.condition(),
                    conditionContext.withEventValue(sd.totalDamage()))) {
                return false;
            }
        }

        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
                    "deals damage", sourcePermanentId));

            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} ON_SELF_DEALS_DAMAGE trigger awaits target ({} damage)",
                    gameData.id, sourceCard.getName(), sd.totalDamage());
            return true;
        }

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

    @CollectsTrigger(value = DealDamageToTargetCreatureDamagedPlayerControlsEffect.class,
            slot = EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)
    private boolean handleAllyInstantOrSorceryDealsDamageToOpponent(TriggerMatchContext match,
            DealDamageToTargetCreatureDamagedPlayerControlsEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.damageToPlayers().isEmpty() || match.permanent() == null) return false;

        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();
        boolean triggered = false;
        for (Map.Entry<UUID, Integer> damageEntry : sd.damageToPlayers().entrySet()) {
            UUID damagedPlayerId = damageEntry.getKey();
            int damage = damageEntry.getValue();
            if (damage <= 0 || damagedPlayerId.equals(match.controllerId())) continue;

            boolean hasCreature = gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of()).stream()
                    .anyMatch(permanent -> gameQueryService.isCreature(gameData, permanent));
            if (!hasCreature) continue;

            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    watcher.getCard(),
                    match.controllerId(),
                    watcher.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
                    damagedPlayerId,
                    watcher.getId());
            entry.setEventValue(damage);
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
            log.info("Game {} - {} triggers for {} damage dealt to opponent {}",
                    gameData.id, watcher.getCard().getName(), damage,
                    gameData.playerIdToName.get(damagedPlayerId));
            triggered = true;
        }
        return triggered;
    }

    /**
     * Blaze Commando: "Whenever an instant or sorcery spell you control deals damage, ...". The
     * source/controller gating is done by the dispatcher; here the watcher permanent simply queues
     * its ability once for the batched damage event.
     */
    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)
    private boolean handleAllyInstantOrSorceryDealsDamage(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;

        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                watcher.getId());
        // Snapshot the damage dealt so an EventValue amount can read it back at resolution.
        entry.setEventValue(sd.totalDamage());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE trigger fires ({} damage from {})",
                gameData.id, watcher.getCard().getName(), sd.totalDamage(), sd.sourceCard().getName());
        return true;
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE)
    @CollectsTrigger(value = CardEffect.class,
            slot = EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER)
    @CollectsTrigger(value = CardEffect.class,
            slot = EffectSlot.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE)
    private boolean handleSelfDealsCombatDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        Card sourceCard = sd.sourceCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            int minTargets = effect instanceof ReturnCardFromGraveyardEffect returnEffect
                    && !returnEffect.upTo() ? 1 : 0;
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, sd.sourceControllerId(), new ArrayList<>(List.of(effect)), null, minTargets, 0));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} ON_SELF_DEALS_COMBAT_DAMAGE trigger awaits graveyard target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, sd.sourceControllerId(), new ArrayList<>(List.of(effect)),
                    !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(sourceCard, effect), sd.totalDamage(),
                    sd.sourcePermanentId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} ON_SELF_DEALS_COMBAT_DAMAGE trigger awaits target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                sd.sourceControllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                sd.sourcePermanentId());
        entry.setEventValue(sd.totalDamage());
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

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE)
    @CollectsTrigger(value = CardEffect.class,
            slot = EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE)
    private boolean handleEquippedCreatureDealsCombatDamage(TriggerMatchContext match, CardEffect effect,
                                                              TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;

        Permanent equipment = match.permanent();
        if (effect.targetSpec().declares(TargetPredicates.anyTarget())) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    equipment.getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    false, null, 0, equipment.getId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE trigger awaits any target",
                    match.gameData().id, equipment.getCard().getName());
            return true;
        }

        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    equipment.getCard(), match.controllerId(), new ArrayList<>(List.of(effect))));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE trigger awaits graveyard target",
                    match.gameData().id, equipment.getCard().getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                equipment.getCard(),
                match.controllerId(),
                equipment.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
                null,
                equipment.getId());
        entry.setNonTargeting(true);
        entry.setEventValue(sd.totalDamage());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(equipment.getCard()));
        log.info("Game {} - {} ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE trigger fires ({} damage by {})",
                match.gameData().id, equipment.getCard().getName(), sd.totalDamage(), sd.sourceCard().getName());
        return true;
    }

    @CollectsTrigger(value = ConditionalEffect.class,
            slot = EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE)
    private boolean handleConditionalEquippedCreatureDealsCombatDamage(TriggerMatchContext match,
                                                                         ConditionalEffect conditional,
                                                                         TriggerContext ctx) {
        if (match.permanent() == null || !conditionEvaluationService.isMet(match.gameData(), conditional.condition(),
                ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }
        return handleEquippedCreatureDealsCombatDamage(match, conditional, ctx);
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

    @CollectsTrigger(value = ConditionalEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageConditional(TriggerMatchContext match,
                                                  ConditionalEffect conditional,
                                                  TriggerContext ctx) {
        if (conditional.interveningIf()
                && !conditionEvaluationService.isMet(match.gameData(), conditional.condition(),
                        ConditionContext.forPermanent(match.permanent(), match.controllerId()))) {
            return false;
        }
        return handleDealtDamageDefault(match, conditional, ctx);
    }

    @CollectsTrigger(value = CardEffect.class, slot = EffectSlot.ON_DEALT_DAMAGE)
    private boolean handleDealtDamageDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            GameData gameData = match.gameData();
            Permanent damagedCreature = dc.damagedCreature();
            UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
            if (controllerId == null) return false;

            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    damagedCreature.getCard(), controllerId, new ArrayList<>(List.of(effect)),
                    !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(damagedCreature.getCard(), effect),
                    dc.damageDealt(), damagedCreature.getId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
            log.info("Game {} - {} ON_DEALT_DAMAGE targeted trigger fires",
                    gameData.id, damagedCreature.getCard().getName());
            return true;
        }
        addDealtDamageEntry(match.gameData(), dc.damagedCreature(), effect, dc.damageDealt());
        return true;
    }

    private TargetFilter targetFilterForTriggeredEffect(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        if (targetIndex >= 0 && targetIndex < card.getSpellTargets().size()) {
            return card.getSpellTargets().get(targetIndex).getFilter();
        }
        return card.getTargetFilter();
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

    private TargetFilter targetFilterFor(Permanent source, CardEffect effect) {
        int targetIndex = source.getCard().getEffectTargetIndex(effect);
        if (targetIndex < 0 || targetIndex >= source.getCard().getSpellTargets().size()) {
            return null;
        }
        return source.getCard().getSpellTargets().get(targetIndex).getFilter();
    }
}
