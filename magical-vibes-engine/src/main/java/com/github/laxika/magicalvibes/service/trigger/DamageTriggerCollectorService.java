package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Trigger collectors for damage-related events (ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE, ON_ANY_PERMANENT_DEALT_DAMAGE,
 * ON_DEALT_DAMAGE, ON_OPPONENT_DEALT_DAMAGE).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class DamageTriggerCollectorService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    /**
     * "Whenever a creature you control fights, [effect]" (Foe-Razer Regent's "put two +1/+1 counters on it at the
     * beginning of the next end step"): the card's effect goes on the stack as written, the fighting creature its
     * triggering permanent ("it", CR 701.14a).
     */
    @CollectsActionTrigger(value = CardEffect.class, watch = ActionWatch.ON_ALLY_CREATURE_FIGHTS)
    private boolean handleAllyCreatureFights(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureFights fights = (TriggerContext.CreatureFights) ctx;
        Permanent fightingCreature = fights.fightingCreature();
        if (fightingCreature == null) return false;

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setNonTargeting(true);
        entry.setTriggeringPermanentId(fightingCreature.getId());
        match.gameData().enqueueTrigger(entry);
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after {} fought",
                match.gameData().id, sourceCard.getName(), fightingCreature.getCard().getName());
        return true;
    }

    /**
     * "Whenever a [filtered] creature you control deals damage to a planeswalker, [effect]" (Hooded Blightfang, Vraska,
     * Swarm's Eminence): the collector judged the trigger's filter on the creature.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_CREATURE_DEALS_DAMAGE_TO_PLANESWALKER)
    private boolean handleAllyCreatureDealsDamageToPlaneswalker(TriggerMatchContext match, TriggerContext ctx) {
        return enqueueAllyCreatureDealsDamageToPlaneswalker(match,
                (TriggerContext.CreatureDealsDamageToPlaneswalker) ctx);
    }

    /** "Whenever a creature you control deals combat damage to a planeswalker, destroy that planeswalker" (Zagras). */
    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLANESWALKER)
    private boolean handleAllyCreatureDealsCombatDamageToPlaneswalker(TriggerMatchContext match, TriggerContext ctx) {
        return enqueueAllyCreatureDealsDamageToPlaneswalker(match,
                (TriggerContext.CreatureDealsDamageToPlaneswalker) ctx);
    }

    private boolean enqueueAllyCreatureDealsDamageToPlaneswalker(TriggerMatchContext match,
            TriggerContext.CreatureDealsDamageToPlaneswalker damageContext) {
        Permanent watcher = match.permanent();
        if (watcher == null || damageContext.damageSource() == null || damageContext.damage() <= 0
                || !gameQueryService.isCreature(match.gameData(), damageContext.damageSource())) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                match.stackEffects(),
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

    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_CREATURE_DEALS_DAMAGE_TO_ENCHANTED_PLANESWALKER)
    private boolean handleCreatureDealsDamageToEnchantedPlaneswalker(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.CreatureDealsDamageToPlaneswalker damageContext =
                (TriggerContext.CreatureDealsDamageToPlaneswalker) ctx;
        Permanent watcher = match.permanent();
        if (watcher == null || damageContext.damageSource() == null || damageContext.damage() <= 0
                || !gameQueryService.isCreature(match.gameData(), damageContext.damageSource())
                || !damageContext.damagedPlaneswalkerId().equals(watcher.getAttachedTo())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                match.stackEffects(),
                damageContext.damageSource().getId(),
                watcher.getId());
        entry.setTriggeringPermanentId(damageContext.damageSource().getId());
        entry.setNonTargeting(true);
        if (damageContext.deferredTriggers() == null) {
            match.gameData().enqueueTrigger(entry);
        } else {
            damageContext.deferredTriggers().add(entry);
        }
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers after a creature dealt damage to an enchanted planeswalker",
                match.gameData().id, watcher.getCard().getName());
        return true;
    }

    /**
     * "Whenever a [filtered] creature is dealt damage, …", whatever the effects are: the entry records the damaged
     * creature as its triggering permanent ("it", "that creature") with its controller as it was dealt the damage
     * ("that creature's controller") and its last known information (CR 608.2h), the damage as its event value ("that
     * much"), the source's snapshot and the "do this only once each turn" mark (CR 603.2h); an ability that chooses no
     * target has the creature as its non-targeting {@code targetId}. A targeted ability, "you may" or not, chooses its
     * target as it goes on the stack (CR 603.3d; the "you may" is answered as it resolves, CR 603.5), through a choice
     * that carries the same record.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE)
    private boolean handleAnyCreatureDealtDamageDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyCreatureDealtDamage dc = (TriggerContext.AnyCreatureDealtDamage) ctx;
        if (dc.damageDealt() <= 0 || match.permanent() == null) return false;

        GameData gameData = match.gameData();
        boolean markOnAcceptance = match.markSourceOncePerTurnOnAcceptance();
        Permanent sourcePermanent = match.permanent();
        Card sourceCard = sourcePermanent.getCard();
        Permanent damaged = dc.damagedCreature();
        Permanent lastKnown = lastKnownDamageSource(gameData, damaged);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            var choice = new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(sourceCard, match.targetingEffect()), dc.damageDealt(),
                    sourcePermanent.getId(), new Permanent(sourcePermanent), hasOptionalTarget(sourceCard,
                            match.targetingEffect()),
                    damaged.getId(), null, match.controllerId(), null, null, false, null, null, null, false,
                    lastKnown)
                    .withEventValue(dc.damageDealt())
                    .withTriggeringPermanentController(dc.damagedCreatureControllerId());
            gameData.queueInteraction(markOnAcceptance ? choice.withMarkSourceOncePerTurnOnAcceptance() : choice);
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} trigger for damage dealt to a creature awaits target",
                    gameData.id, sourceCard.getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                sourcePermanent.getId()
        );
        entry.setMarkSourceOncePerTurnOnAcceptance(markOnAcceptance);
        entry.setTargetId(damaged.getId());
        entry.setEventValue(dc.damageDealt());
        entry.setTriggeringPermanentId(damaged.getId());
        entry.setTriggeringPermanentControllerId(dc.damagedCreatureControllerId());
        entry.setTriggeringPermanent(lastKnown);
        entry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for damage dealt to a creature", gameData.id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever a permanent you control is dealt damage, …" ("a Dinosaur you control", "this or a creature it's paired
     * with": the trigger's filter on the damaged permanent, the relation judged by the collector), whatever the effects
     * are: the ability is the watcher's (CR 113.7), and its entry records the damaged permanent as its triggering
     * permanent with its controller, its power and its last known information with the damage keywords it had ("it
     * deals that much damage", CR 608.2h), the damage as its event value and X ("that much"), and the source's
     * snapshot. A targeted ability chooses its target as it goes on the stack (CR 603.3d), through a choice that
     * carries the same record.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE)
    private boolean handleAnyPermanentDealtDamageDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AnyPermanentDealtDamage dc = (TriggerContext.AnyPermanentDealtDamage) ctx;
        if (dc.damageDealt() <= 0 || match.permanent() == null || match.controllerId() == null) return false;

        GameData gameData = match.gameData();
        Permanent sourcePermanent = match.permanent();
        Card sourceCard = sourcePermanent.getCard();
        Permanent damaged = dc.damagedPermanent();
        Permanent lastKnown = lastKnownDamageSource(gameData, damaged);
        int power = gameQueryService.getEffectivePower(gameData, damaged);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            TargetFilter targetFilter = targetFilterForTriggeredEffect(sourceCard, match.targetingEffect());
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT)
                            || targetFilter instanceof PlayerPredicateTargetFilter,
                    targetFilter, dc.damageDealt(), sourcePermanent.getId(), new Permanent(sourcePermanent),
                    hasOptionalTarget(sourceCard,
                            match.targetingEffect()), damaged.getId(), null, match.controllerId(), null, null,
                    false, null, null, power, false, lastKnown)
                    .withEventValue(dc.damageDealt())
                    .withTriggeringPermanentController(dc.damagedPermanentControllerId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} trigger for damage dealt to a permanent you control awaits target",
                    gameData.id, sourceCard.getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                sourcePermanent.getId());
        entry.setEventValue(dc.damageDealt());
        entry.setTriggeringPermanentId(damaged.getId());
        entry.setTriggeringCardId(damaged.getCard().getId());
        entry.setTriggeringPermanentControllerId(dc.damagedPermanentControllerId());
        entry.setTriggeringPermanent(lastKnown);
        entry.setTriggeringPermanentPowerAtTrigger(power);
        entry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for damage dealt to a permanent you control", gameData.id,
                sourceCard.getName());
        return true;
    }

    /**
     * "Whenever a source you control deals noncombat damage to a creature, …", whatever the effects are: the entry
     * records the creature as its triggering permanent with its controller and its last known information, the damage
     * as its event value and the source's snapshot. Taii Wakeen's "equal to that creature's toughness" is its event
     * condition, judged as the damage is dealt; Crude Abattoir's "if the second door is unlocked" its intervening "if"
     * (CR 603.4). A targeted ability chooses its target as it goes on the stack (CR 603.3d) with the same record.
     */
    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE)
    private boolean handleAllySourceDealsNoncombatDamageToCreatureDefault(TriggerMatchContext match,
            TriggerContext ctx) {
        TriggerContext.SourceDealsNoncombatDamageToCreature dc =
                (TriggerContext.SourceDealsNoncombatDamageToCreature) ctx;
        if (dc.damageDealt() <= 0 || match.permanent() == null || dc.damagedCreature() == null) return false;

        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();
        Card sourceCard = watcher.getCard();
        Permanent damaged = dc.damagedCreature();
        UUID damagedControllerId = gameQueryService.findPermanentController(gameData, damaged.getId());
        Permanent lastKnown = lastKnownDamageSource(gameData, damaged);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            TargetFilter targetFilter = targetFilterForTriggeredEffect(sourceCard, match.targetingEffect());
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT)
                            || targetFilter instanceof PlayerPredicateTargetFilter,
                    targetFilter, dc.damageDealt(), watcher.getId(), new Permanent(watcher),
                    hasOptionalTarget(sourceCard,
                            match.targetingEffect()), damaged.getId(), null, match.controllerId(), null, null,
                    false, null, null, null, false, lastKnown)
                    .withEventValue(dc.damageDealt())
                    .withTriggeringPermanentController(damagedControllerId));
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                watcher.getId());
        entry.setEventValue(dc.damageDealt());
        entry.setTriggeringPermanentId(damaged.getId());
        entry.setTriggeringCardId(damaged.getCard().getId());
        entry.setTriggeringPermanentControllerId(damagedControllerId);
        entry.setTriggeringPermanent(lastKnown);
        entry.setSourcePermanentSnapshot(new Permanent(watcher));
        entry.setNonTargeting(true);
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after a source its controller controls dealt noncombat damage to a creature",
                gameData.id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever enchanted (equipped) creature is dealt damage, …": the ability is the Aura's or Equipment's (CR 113.7),
     * whatever its effects are. Its entry records the event: the creature as the triggering permanent (its id,
     * controller, power and a snapshot with the damage keywords it had, its last known information once it has left,
     * CR 608.2h) and the damage dealt as the event value; "it" / "its controller" / "that much" read them as the
     * ability resolves. A targeted ability's target is chosen as it goes on the stack (CR 603.3d).
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ENCHANTED_CREATURE_DEALT_DAMAGE)
    private boolean handleEnchantedCreatureDealtDamageDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent aura = match.permanent();
        Permanent creature = dc.damagedCreature();
        Permanent lastKnown = lastKnownDamageSource(gameData, creature);
        int power = gameQueryService.getEffectivePower(gameData, creature);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    aura.getCard(), match.controllerId(), match.stackEffects(), false,
                    targetFilterForTriggeredEffect(aura.getCard(),
                            match.targetingEffect()), 0, aura.getId(), new Permanent(aura),
                    false, creature.getId(), null, match.controllerId(), null, null, false, null, null, power,
                    false, lastKnown).withEventValue(dc.damageDealt()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    match.controllerId(),
                    aura.getCard().getName() + "'s ability",
                    match.stackEffects(),
                    null,
                    aura.getId());
            entry.setEventValue(dc.damageDealt());
            entry.setTriggeringPermanentId(creature.getId());
            entry.setTriggeringCardId(creature.getCard().getId());
            entry.setTriggeringPermanentControllerId(gameQueryService.findPermanentController(
                    gameData, creature.getId()));
            entry.setTriggeringPermanent(lastKnown);
            entry.setTriggeringPermanentPowerAtTrigger(power);
            entry.setSourcePermanentSnapshot(new Permanent(aura));
            entry.setNonTargeting(true);
            gameData.enqueueTrigger(entry);
        }

        gameLogService.append(gameData, GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_CREATURE_DEALT_DAMAGE trigger fires ({} damage)",
                gameData.id, aura.getCard().getName(), dc.damageDealt());
        return true;
    }

    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_ALLY_SOURCE)
    private boolean handleControllerDealtDamageByAllySource(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();

        if (match.admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    perm.getCard(), match.controllerId(), match.stackEffects(), true,
                    targetFilterForTriggeredEffect(perm.getCard(),
                            match.targetingEffect()), dc.amount(), perm.getId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} controlled-source damage trigger awaits a target", gameData.id,
                    perm.getCard().getName());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(),
                match.controllerId(),
                perm.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                perm.getId());
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} controlled-source damage trigger fires ({} damage)", gameData.id,
                perm.getCard().getName(), dc.amount());
        return true;
    }

    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_CONTROLLER_DAMAGE_PREVENTED)
    private boolean handleControllerDamagePreventedDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(), match.controllerId(), perm.getCard().getName() + "'s ability",
                match.stackEffects(), null, perm.getId());
        entry.setEventValue(dc.amount());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
        log.info("Game {} - {} ON_CONTROLLER_DAMAGE_PREVENTED trigger fires ({} damage prevented)",
                gameData.id, perm.getCard().getName(), dc.amount());
        return true;
    }

    /**
     * The default of the damage-to-a-player watches whose "that player" is the player dealt damage: "whenever you're
     * dealt damage", "whenever an opponent is dealt damage", "whenever a source you control deals damage to an opponent
     * (a player)", a plane's "whenever a creature deals damage to its controller", "whenever a source you control deals
     * noncombat damage to an opponent" and "whenever an opponent is dealt noncombat damage". The ability goes on the
     * stack, or to its target choice, with the event recorded whatever its effects are (see
     * {@link #queuePlayerDamageAbility}). The collectors judged the trigger's relation, filter, event condition and
     * intervening "if" as it triggered.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_CONTROLLER_DEALT_DAMAGE)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_OPPONENT_DEALT_DAMAGE)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_SOURCE_DEALS_DAMAGE_TO_PLAYER)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_SOURCE_DEALS_DAMAGE_TO_ITS_CONTROLLER_PLANAR)
    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_OPPONENT)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_OPPONENT_DEALT_NONCOMBAT_DAMAGE)
    private boolean handlePlayerDamageDefault(TriggerMatchContext match, TriggerContext ctx) {
        PlayerDamage damage = PlayerDamage.of(ctx);
        return queuePlayerDamageAbility(match, damage, damage.damagedPlayerId());
    }

    /**
     * The default of the watches of a source an opponent controls dealing damage: "whenever a source an opponent
     * controls deals damage to you" and "… to you or a permanent you control". "That player" (Michiko Konda's "that
     * player sacrifices a permanent", Elesh Norn's "that source's controller loses 2 life unless they pay {1}") is the
     * opponent who controlled the source as it dealt the damage; the rest of the record is
     * {@link #queuePlayerDamageAbility}'s.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT)
    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_OPPONENT_SOURCE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT)
    private boolean handleOpponentSourceDamageDefault(TriggerMatchContext match, TriggerContext ctx) {
        PlayerDamage damage = PlayerDamage.of(ctx);
        return queuePlayerDamageAbility(match, damage, damage.sourceControllerId());
    }

    /**
     * Puts a damage-to-a-player ability on the stack with the event recorded whatever its effects are: the damage as
     * its event value ("that much", "that many"), the player dealt damage as its triggering player, the source of the
     * damage, when it is a permanent, as its triggering permanent and the source's controller as it dealt the damage
     * as that permanent's controller ("that source's controller", the fallback once it has left and the controller of
     * a spell source), the ability's source as it was (or the face-up plane) and its "do this only once each turn"
     * mark (CR 603.2h). An ability that chooses no target has {@code thatPlayerId} as its non-targeting "that player";
     * a player the card declares no target for is "that player" too (Pain Magnification's "that player discards a
     * card"). A target the card declares is chosen as the ability goes on the stack (CR 603.3d), its "that player
     * controls" judged against the player dealt damage, the choice carrying the damage and the player into the entry.
     */
    private boolean queuePlayerDamageAbility(TriggerMatchContext match, PlayerDamage damage,
            UUID thatPlayerId) {
        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();
        Card sourceCard = watcher != null ? watcher.getCard() : match.sourceCard();
        if (sourceCard == null) return false;
        UUID sourcePermanentId = watcher == null ? null : watcher.getId();
        boolean thatPlayer = isThatPlayer(sourceCard, match);
        if (!thatPlayer && (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    sourceCard, match.controllerId(), match.effects(), sourcePermanentId, match.controllerId(),
                    damage.damagedPlayerId(), damage.sourcePermanentId(), damage.amount())
                    .withEventValue(damage.amount()).withTriggeringPlayer(damage.damagedPlayerId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} triggers for {} damage to a player and needs a target",
                    gameData.id, sourceCard.getName(), damage.amount());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                sourcePermanentId);
        if (thatPlayer || !match.declaresTarget()) {
            entry.setTargetId(thatPlayerId);
            entry.setNonTargeting(true);
        }
        entry.setEventValue(damage.amount());
        entry.setTriggeringPlayerId(damage.damagedPlayerId());
        entry.setTriggeringPermanentId(damage.sourcePermanentId());
        entry.setTriggeringPermanentControllerId(damage.sourceControllerId());
        if (watcher != null) {
            entry.setSourcePermanentSnapshot(new Permanent(watcher));
        }
        if (match.sourcePlanarObject() != null) {
            entry.setSourcePlanarObject(match.sourcePlanarObject().copy());
        }
        entry.setMarkSourceOncePerTurnOnAcceptance(match.markSourceOncePerTurnOnAcceptance());
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers for {} damage to a player", gameData.id, sourceCard.getName(),
                damage.amount());
        return true;
    }

    /**
     * Whether the player of {@code match}'s ability is the event's "that player" rather than a target: an effect admits
     * a player, and every effect that admits a player or a permanent is one whose player is "that player"
     * ({@link #isThatPlayer(Card, CardEffect)}).
     */
    private static boolean isThatPlayer(Card card, TriggerMatchContext match) {
        List<CardEffect> targeting = match.effects().stream()
                .filter(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .toList();
        return !targeting.isEmpty() && targeting.stream().allMatch(effect -> isThatPlayer(card, effect));
    }

    /**
     * Whether {@code effect}'s player is the event's "that player" rather than a target: it admits a player but no
     * permanent, and neither the card nor the effect declares a target for it.
     */
    private static boolean isThatPlayer(Card card, CardEffect effect) {
        return effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                && !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                && !card.hasEffectTargetIndex(effect)
                && !(effect instanceof DamageDealingEffect damageEffect && damageEffect.triggeredTargetFilter() != null);
    }

    /**
     * A damage-to-a-player event as its collectors report it: the player dealt damage, the damage (0 where the event
     * names none), the source as a permanent (or {@code null}) and the source's controller as it dealt the damage (or
     * {@code null} where the event names none).
     */
    private record PlayerDamage(UUID damagedPlayerId, int amount, UUID sourcePermanentId, UUID sourceControllerId) {

        static PlayerDamage of(TriggerContext ctx) {
            return switch (ctx) {
                case TriggerContext.DamageToControllerAmount d ->
                        new PlayerDamage(d.damagedPlayerId(), d.amount(), d.sourcePermanentId(), d.sourceControllerId());
                case TriggerContext.NoncombatDamageToOpponent n ->
                        new PlayerDamage(n.damagedPlayerId(), n.damageAmount(), null, n.sourceControllerId());
                case TriggerContext.SourceDamageToYouOrYourPermanent s ->
                        new PlayerDamage(s.damagedPlayerId(), 0, s.sourcePermanentId(), s.sourceControllerId());
                default -> throw new IllegalArgumentException("Not damage dealt to a player: " + ctx);
            };
        }
    }

    /**
     * "Whenever one or more [filtered] creatures you control deal damage to a player [an opponent], [effect]" (Francisco,
     * Fowl Marauder, The Thing, Ben Grimm, Breeches, Brazen Plunderer, Malcolm, Keen-Eyed Navigator): the collector
     * judged the trigger's filter on the creatures that dealt damage. The ability triggers once for the event.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_PLAYER)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_CREATURES_DEAL_DAMAGE_TO_OPPONENT)
    private boolean handleAllyCreaturesDealDamageToPlayer(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.AllyCreaturesDealDamageToPlayer damageContext =
                (TriggerContext.AllyCreaturesDealDamageToPlayer) ctx;
        Permanent watcher = match.permanent();
        if (watcher == null || !match.controllerId().equals(damageContext.sourceControllerId())) {
            return false;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                match.stackEffects(),
                damageContext.damagedPlayerId(),
                watcher.getId());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} triggers after one or more controlled creatures dealt damage to a player",
                match.gameData().id, watcher.getCard().getName());
        return true;
    }

    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ENCHANTED_PLAYER_DEALT_DAMAGE)
    private boolean handleEnchantedPlayerDealtDamage(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        if (dc.amount() <= 0) return false;

        Permanent aura = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                match.controllerId(),
                aura.getCard().getName() + "'s ability",
                match.stackEffects(),
                dc.damagedPlayerId(),
                aura.getId());
        entry.setEventValue(dc.amount());
        entry.setNonTargeting(true);
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(aura.getCard()));
        log.info("Game {} - {} ON_ENCHANTED_PLAYER_DEALT_DAMAGE trigger fires ({} damage)",
                match.gameData().id, aura.getCard().getName(), dc.amount());
        return true;
    }

    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_SELF_DEALS_DAMAGE)
    private boolean handleSelfDealsDamage(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        GameData gameData = match.gameData();
        Card sourceCard = sd.sourceCard();
        // The source may have died dealing the damage; keep its last-known permanent id when present.
        UUID sourcePermanentId = sd.sourcePermanentId();
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)
                || match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), match.stackEffects(),
                    "deals damage", sourcePermanentId, sd.totalDamage()));

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
                match.stackEffects(),
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

    /**
     * Blaze Commando: "Whenever an instant or sorcery spell you control deals damage, ...". The
     * source/controller gating is done by the dispatcher; here the watcher permanent simply queues
     * its ability once for the batched damage event.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)
    private boolean handleAllyInstantOrSorceryDealsDamage(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;

        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                match.stackEffects(),
                null,
                watcher.getId());
        // Snapshot the damage dealt so an EventValue amount can read it back at resolution.
        entry.setEventValue(sd.totalDamage());
        // Snapshot the watcher so a transform instruction can tell whether it has already transformed
        // since this trigger was put on the stack (CR 701.27f).
        entry.setSourcePermanentSnapshot(new Permanent(watcher));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
        log.info("Game {} - {} ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE trigger fires ({} damage from {})",
                gameData.id, watcher.getCard().getName(), sd.totalDamage(), sd.sourceCard().getName());
        return true;
    }

    @CollectsCombatTrigger(value = CardEffect.class, watch = CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE)
    @CollectsCombatTrigger(value = CardEffect.class,
            watch = CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_PLANESWALKER)
    @CollectsCombatTrigger(value = CardEffect.class,
            watch = CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE)
    private boolean handleSelfDealsCombatDamage(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        Card sourceCard = sd.sourceCard();
        ChooseOneAtTriggerTimeEffect modal = match.modal(effect ->
                effect instanceof ChooseOneAtTriggerTimeEffect triggerTime ? triggerTime : null);
        if (modal != null) {
            // A modal ability chooses its modes as it goes on the stack (CR 603.3c), as every reader routes one.
            match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                    sourceCard, sd.sourceControllerId(), modal.choice(), sd.sourcePermanentId()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} combat damage trigger chooses modes as it is put on the stack",
                    match.gameData().id, sourceCard.getName());
            return true;
        }
        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            ReturnCardFromGraveyardEffect returnEffect = match.effectOf(ReturnCardFromGraveyardEffect.class);
            int minTargets = returnEffect != null && !returnEffect.upTo() ? 1 : 0;
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, sd.sourceControllerId(), match.stackEffects(), null, minTargets, 0,
                    match.permanent() != null && match.permanent().isAlternateCost()));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} ON_SELF_DEALS_COMBAT_DAMAGE trigger awaits graveyard target",
                    match.gameData().id, sourceCard.getName());
            return true;
        }
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, sd.sourceControllerId(), match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(sourceCard, match.targetingEffect()), sd.totalDamage(),
                    sd.sourcePermanentId(), hasOptionalTarget(sourceCard, match.targetingEffect())));
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
                match.stackEffects(),
                null,
                sd.sourcePermanentId());
        entry.setEventValue(sd.totalDamage());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} ON_SELF_DEALS_COMBAT_DAMAGE trigger fires ({} damage)",
                match.gameData().id, sourceCard.getName(), sd.totalDamage());
        return true;
    }

    @CollectsCombatTrigger(value = CardEffect.class, watch = CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE)
    private boolean handleAllyCreatureDealsCombatDamage(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;

        Card watcherCard = match.permanent().getCard();
        // The watcher, not the damage dealer, is the trigger's source permanent so a self-referencing
        // effect (Five-Alarm Fire's blaze counter) lands on the watcher. The damage dealer is the
        // triggering permanent so a TRIGGERING-referenced effect (Quilled Greatwurm's counters) lands on it.
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcherCard,
                match.controllerId(),
                watcherCard.getName() + "'s ability",
                match.stackEffects(),
                null,
                match.permanent().getId());
        entry.setTriggeringPermanentId(sd.sourcePermanentId());
        entry.setNonTargeting(true);
        entry.setEventValue(sd.totalDamage());
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(watcherCard));
        log.info("Game {} - {} ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE trigger fires ({} damage by {})",
                match.gameData().id, watcherCard.getName(), sd.totalDamage(), sd.sourceCard().getName());
        return true;
    }

    @CollectsCombatTrigger(value = CardEffect.class, watch = CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE)
    @CollectsCombatTrigger(value = CardEffect.class,
            watch = CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE)
    private boolean handleEquippedCreatureDealsCombatDamage(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        return queueEquippedCreatureCombatDamageTrigger(match, sd, sd.totalDamage());
    }

    /**
     * "Whenever equipped creature deals combat damage to a player, ... that much" counts only the
     * damage dealt to players, not what the same creature assigned to blockers.
     */
    @CollectsCombatTrigger(value = CardEffect.class, watch = CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER)
    private boolean handleEquippedCreatureDealsCombatDamageToPlayer(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        return queueEquippedCreatureCombatDamageTrigger(match, sd, sd.damageToPlayers());
    }

    private boolean queueEquippedCreatureCombatDamageTrigger(TriggerMatchContext match,
            TriggerContext.SourceDealsCombatDamage sd,
                                                               int damage) {
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;

        Permanent equipment = match.permanent();
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            boolean optionalTarget = equipment.getCard().getTargetGroups().size() == 1
                    && equipment.getCard().getTargetGroups().getFirst().getMinTargets() == 0
                    && equipment.getCard().getTargetGroups().getFirst().getMaxTargets() == 1
                    && equipment.getCard().getEffectTargetIndex(match.targetingEffect())
                    == equipment.getCard().getTargetGroups().getFirst().getIndex();
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    equipment.getCard(), match.controllerId(), match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(equipment.getCard(),
                            match.targetingEffect()), damage, equipment.getId(),
                    optionalTarget));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE trigger awaits target",
                    match.gameData().id, equipment.getCard().getName());
            return true;
        }

        if (match.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    equipment.getCard(), match.controllerId(), match.stackEffects()));
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
                match.stackEffects(),
                null,
                equipment.getId());
        // "That player" is the player dealt damage, if any: the entry's triggering player and non-targeting target.
        entry.setTargetId(sd.damagedPlayerId());
        entry.setTriggeringPlayerId(sd.damagedPlayerId());
        entry.setTriggeringPermanentId(sd.sourcePermanentId());
        entry.setNonTargeting(true);
        entry.setEventValue(damage);
        entry.setSourcePermanentSnapshot(new Permanent(equipment));
        match.gameData().enqueueTrigger(entry);

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(equipment.getCard()));
        log.info("Game {} - {} ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE trigger fires ({} damage by {})",
                match.gameData().id, equipment.getCard().getName(), damage, sd.sourceCard().getName());
        return true;
    }

    /**
     * "Whenever this is dealt damage, …" and "… noncombat damage, …", in or out of combat, whatever the effects are: the
     * ability is the damaged permanent's (CR 113.7), and its entry records the event. The damage is its event value
     * ("that much", CR 120.8: no damage, no trigger), the source of the damage its triggering permanent (if it was
     * one) and that source's controller as it dealt the damage its triggering permanent's controller ("that source's
     * controller"), and the permanent as it was dealt the damage, with the damage keywords it had, its source snapshot
     * ("it deals that much damage" once it has died, CR 608.2h). A targeted ability chooses its target as it goes on the
     * stack (CR 603.3d), through a choice that carries the same record and the damage as X; its target filter is the
     * one the card declares for the effect, else the effect's own ({@link #targetFilterForTriggeredEffect}).
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_DEALT_DAMAGE)
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_NONCOMBAT_DAMAGE_TO_SELF)
    private boolean handleDealtDamageDefault(TriggerMatchContext match, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        GameData gameData = match.gameData();
        Permanent damaged = dc.damagedCreature();
        UUID controllerId = match.controllerId();
        if (dc.damageDealt() <= 0 || damaged == null || controllerId == null) return false;

        Card card = damaged.getCard();
        Permanent lastKnown = dc.damagedLastKnown() != null
                ? dc.damagedLastKnown() : lastKnownDamageSource(gameData, damaged);
        if (match.admits(TargetPredicate.Kind.PERMANENT)
                || match.admits(TargetPredicate.Kind.PLAYER)) {
            TargetFilter targetFilter = targetFilterForTriggeredEffect(card, match.targetingEffect());
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    card, controllerId, match.stackEffects(),
                    !match.admits(TargetPredicate.Kind.PERMANENT)
                            || targetFilter instanceof PlayerPredicateTargetFilter,
                    targetFilter, dc.damageDealt(), damaged.getId(), lastKnown, hasOptionalTarget(card,
                            match.targetingEffect()),
                    dc.sourcePermanentId(), null, controllerId, null, null, false, null, null, null, false, null)
                    .withEventValue(dc.damageDealt())
                    .withTriggeringPermanentController(dc.damageSourceControllerId()));
            gameLogService.append(gameData, GameLog.abilityTriggers(card));
            log.info("Game {} - {} dealt-damage trigger awaits target ({} damage)",
                    gameData.id, card.getName(), dc.damageDealt());
            return true;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                controllerId,
                card.getName() + "'s ability",
                match.stackEffects(),
                null,
                damaged.getId());
        entry.setEventValue(dc.damageDealt());
        entry.setTriggeringPermanentId(dc.sourcePermanentId());
        entry.setTriggeringPermanentControllerId(dc.damageSourceControllerId());
        entry.setSourcePermanentSnapshot(lastKnown);
        gameData.enqueueTrigger(entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(card));
        log.info("Game {} - {} dealt-damage trigger fires ({} damage)", gameData.id, card.getName(),
                dc.damageDealt());
        return true;
    }

    /**
     * The target filter of a triggered ability's target choice: the one the card declares for {@code effect}, else the
     * effect's triggered filter, else the card's. With none, an effect whose player half is "target opponent"
     * ({@link CardEffect#targetPlayerRelation()}: "target opponent or planeswalker") is offered the opponents only, and
     * its permanent half (the planeswalkers) as its target predicate restricts it: the choice reads no relation of its
     * own.
     */
    static TargetFilter targetFilterForTriggeredEffect(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        if (targetIndex >= 0 && targetIndex < card.getTargetGroups().size()) {
            return card.getTargetGroups().get(targetIndex).getFilter();
        }
        if (effect instanceof DamageDealingEffect damageEffect
                && damageEffect.triggeredTargetFilter() != null) {
            return damageEffect.triggeredTargetFilter();
        }
        TargetFilter cardFilter = card.getTargetFilter();
        if (cardFilter != null || effect.targetPlayerRelation() != PlayerRelation.OPPONENT
                || !effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            return cardFilter;
        }
        PlayerRelationPredicate opponent = new PlayerRelationPredicate(PlayerRelation.OPPONENT);
        if (!effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
            return new PlayerPredicateTargetFilter(opponent, "Target must be an opponent");
        }
        return new AnyTargetPredicateTargetFilter(
                effect.targetSpec().targetPredicate().permanentRestriction().orElse(new PermanentTruePredicate()),
                opponent, "Target must be an opponent or planeswalker");
    }

    static boolean hasOptionalTarget(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        return targetIndex >= 0 && targetIndex < card.getTargetGroups().size()
                && card.getTargetGroups().get(targetIndex).getMinTargets() == 0;
    }

    /**
     * Last known information for a creature whose own ability deals damage after it may have died:
     * the damage-relevant keywords it had from other permanents' static abilities are baked in.
     */
    private Permanent lastKnownDamageSource(com.github.laxika.magicalvibes.model.GameData gameData,
                                            Permanent creature) {
        Permanent snapshot = new Permanent(creature);
        for (com.github.laxika.magicalvibes.model.Keyword keyword : List.of(
                com.github.laxika.magicalvibes.model.Keyword.LIFELINK,
                com.github.laxika.magicalvibes.model.Keyword.DEATHTOUCH,
                com.github.laxika.magicalvibes.model.Keyword.INFECT,
                com.github.laxika.magicalvibes.model.Keyword.WITHER)) {
            if (gameQueryService.hasKeyword(gameData, creature, keyword)) {
                snapshot.getGrantedKeywords().add(keyword);
            }
        }
        return snapshot;
    }
}
