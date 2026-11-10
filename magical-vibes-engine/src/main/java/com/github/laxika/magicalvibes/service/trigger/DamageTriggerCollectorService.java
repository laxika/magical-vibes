package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfCombatDamageSourceIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.DamageRecipient;
import com.github.laxika.magicalvibes.model.effect.DamageDealingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerOrPlaneswalkerEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostCreatureCardInHandEffect;
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
    private boolean handleAllyCreatureFights(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.CreatureFights fights = (TriggerContext.CreatureFights) ctx;
        Permanent fightingCreature = fights.fightingCreature();
        if (fightingCreature == null) return false;

        Card sourceCard = match.permanent().getCard();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                match.controllerId(),
                sourceCard.getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleAllyCreatureDealsDamageToPlaneswalker(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        return enqueueAllyCreatureDealsDamageToPlaneswalker(match, effect,
                (TriggerContext.CreatureDealsDamageToPlaneswalker) ctx);
    }

    /** "Whenever a creature you control deals combat damage to a planeswalker, destroy that planeswalker" (Zagras). */
    @CollectsDamageTrigger(value = CardEffect.class,
            watch = DamageWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLANESWALKER)
    private boolean handleAllyCreatureDealsCombatDamageToPlaneswalker(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        return enqueueAllyCreatureDealsDamageToPlaneswalker(match, effect,
                (TriggerContext.CreatureDealsDamageToPlaneswalker) ctx);
    }

    private boolean enqueueAllyCreatureDealsDamageToPlaneswalker(TriggerMatchContext match, CardEffect effect,
            TriggerContext.CreatureDealsDamageToPlaneswalker damageContext) {
        Permanent watcher = match.permanent();
        if (watcher == null || damageContext.damageSource() == null || damageContext.damage() <= 0
                || !gameQueryService.isCreature(match.gameData(), damageContext.damageSource())) return false;

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                watcher.getCard(),
                match.controllerId(),
                watcher.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleCreatureDealsDamageToEnchantedPlaneswalker(TriggerMatchContext match,
            CardEffect trigger, TriggerContext ctx) {
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
                new ArrayList<>(List.of(trigger)),
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

    @CollectsDamageTrigger(value = DrawCardEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE)
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

    @CollectsDamageTrigger(value = ConditionalEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE)
    private boolean handleAllySourceDealsNoncombatDamageToCreatureConditional(TriggerMatchContext match,
            ConditionalEffect conditional, TriggerContext ctx) {
        if (match.permanent() == null) {
            return false;
        }
        return enqueueAllySourceDealsNoncombatDamageToCreature(match, conditional, ctx);
    }

    @CollectsDamageTrigger(value = PerpetuallyBoostCreatureCardInHandEffect.class,
            watch = DamageWatch.ON_ALLY_SOURCE_DEALS_NONCOMBAT_DAMAGE_TO_CREATURE)
    private boolean handleAllySourceDealsNoncombatDamageToCreatureBoost(TriggerMatchContext match,
            PerpetuallyBoostCreatureCardInHandEffect effect, TriggerContext ctx) {
        return enqueueAllySourceDealsNoncombatDamageToCreature(match, effect, ctx);
    }

    private boolean enqueueAllySourceDealsNoncombatDamageToCreature(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsNoncombatDamageToCreature damageContext =
                (TriggerContext.SourceDealsNoncombatDamageToCreature) ctx;
        Permanent damagedCreature = damageContext.damagedCreature();
        GameData gameData = match.gameData();
        if (match.permanent() == null || damagedCreature == null
                || !gameQueryService.isCreature(gameData, damagedCreature)
                || damageContext.damageDealt() <= 0) {
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
        entry.setSourcePermanentSnapshot(new Permanent(match.permanent()));
        gameData.enqueueTrigger(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers after an ally source dealt noncombat damage to a creature",
                gameData.id, match.permanent().getCard().getName());
        return true;
    }

    /**
     * Archfiend of Spite's "whenever a source an opponent controls deals damage to this creature": the source's
     * controller as the damage was dealt must be an opponent. The default queues the ability, with the source as the
     * triggering permanent the effect reads.
     */
    @CollectsDamageTrigger(value = DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect.class,
            watch = DamageWatch.ON_DEALT_DAMAGE)
    private boolean handleOpponentDamageSourceDealtDamage(TriggerMatchContext match,
            DamageSourceControllerLosesLifeUnlessSacrificesPermanentsEffect trigger, TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageSourceControllerId() == null
                || dc.damageSourceControllerId().equals(match.controllerId())) {
            return false;
        }
        return handleDealtDamageDefault(match, trigger, ctx);
    }

    @CollectsDamageTrigger(value = DealDamageToTargetPlayerOrPlaneswalkerEffect.class, watch = DamageWatch.ON_DEALT_DAMAGE)
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
                    dc.damageDealt(),
                    dc.damagedCreature().getId(),
                    null,
                    false,
                    null,
                    null,
                    gameQueryService.findPermanentController(gameData, dc.damagedCreature().getId()),
                    null));
            gameLogService.append(gameData, GameLog.abilityTriggers(dc.damagedCreature().getCard()));
            log.info("Game {} - {} ON_DEALT_DAMAGE target trigger fires",
                    gameData.id, dc.damagedCreature().getCard().getName());
            return true;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, dc.damagedCreature().getId());
        if (controllerId == null) return false;

        // "Target opponent or planeswalker" is chosen as the ability goes on the stack (CR 603.3d), among every
        // opponent and planeswalker, even when only one is legal.
        TargetFilter declaredFilter = targetFilterFor(dc.damagedCreature(), trigger);
        TargetFilter targetFilter = declaredFilter != null ? declaredFilter : new AnyTargetPredicateTargetFilter(
                new PermanentIsPlaneswalkerPredicate(),
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent or planeswalker");
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                dc.damagedCreature().getCard(), controllerId, new ArrayList<>(List.of(trigger)),
                false, targetFilter, dc.damageDealt(), dc.damagedCreature().getId(),
                lastKnownDamageSource(gameData, dc.damagedCreature())
        ));

        gameLogService.append(gameData, GameLog.abilityTriggers(dc.damagedCreature().getCard()));
        log.info("Game {} - {} ON_DEALT_DAMAGE target-opponent-or-planeswalker trigger fires",
                gameData.id, dc.damagedCreature().getCard().getName());
        return true;
    }

    @CollectsDamageTrigger(value = DealDamageToAnyTargetEffect.class, watch = DamageWatch.ON_DEALT_DAMAGE)
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
        TargetFilter targetFilter = trigger.triggeredTargetFilter();
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                damagedCreature.getCard(), controllerId, new ArrayList<>(List.of(trigger)),
                false, targetFilter, dc.damageDealt(), damagedCreature.getId(),
                lastKnownDamageSource(gameData, damagedCreature)));

        gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
        log.info("Game {} - {} ON_DEALT_DAMAGE deal-damage-to-any-target trigger fires",
                gameData.id, damagedCreature.getCard().getName());
        return true;
    }

    @CollectsDamageTrigger(value = DealDamageToAnyTargetEffect.class,
            watch = DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE)
    private boolean handleAnyPermanentDealtDamageToAnyTarget(TriggerMatchContext match,
            DealDamageToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.AnyPermanentDealtDamage dc = (TriggerContext.AnyPermanentDealtDamage) ctx;
        return queueAnyPermanentDealtDamageTrigger(match, trigger, dc);
    }

    private boolean queueAnyPermanentDealtDamageTrigger(TriggerMatchContext match,
            CardEffect trigger, TriggerContext.AnyPermanentDealtDamage dc) {
        if (dc.damageDealt() <= 0 || match.permanent() == null || match.controllerId() == null
                || !match.controllerId().equals(dc.damagedPermanentControllerId())) return false;

        Permanent sourcePermanent = match.permanent();
        Card sourceCard = sourcePermanent.getCard();
        // The ability is its source's (CR 113.7); the choice records the damaged permanent as the triggering
        // permanent, with its damage keywords as it was dealt the damage, for "it deals that much damage"
        // (Wrathful Raptors, Wrathful Red Dragon: DealDamageToAnyTargetEffect.dealtByTriggeringPermanent).
        Permanent damaged = dc.damagedPermanent();
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), false,
                targetFilterForTriggeredEffect(sourceCard, trigger), dc.damageDealt(),
                sourcePermanent.getId(), new Permanent(sourcePermanent), false, damaged.getId(), null,
                match.controllerId(), null, null, false, null, null,
                gameQueryService.getEffectivePower(match.gameData(), damaged), false,
                lastKnownDamageSource(match.gameData(), damaged)).withEventValue(dc.damageDealt()));

        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} ON_ANY_PERMANENT_DEALT_DAMAGE deal-damage-to-any-target trigger fires",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    /**
     * "Whenever this or a creature it's paired with is dealt damage, it deals that much damage to target player"
     * (Donna Noble): the collector judged the trigger's filter on the damaged permanent.
     */
    @CollectsDamageTrigger(value = DealDamageToPlayersEffect.class, watch = DamageWatch.ON_ANY_PERMANENT_DEALT_DAMAGE)
    private boolean handleAnyPermanentDealtDamageToPlayer(TriggerMatchContext match,
            DealDamageToPlayersEffect damage, TriggerContext ctx) {
        TriggerContext.AnyPermanentDealtDamage dc = (TriggerContext.AnyPermanentDealtDamage) ctx;
        if (dc.damageDealt() <= 0 || match.permanent() == null || match.controllerId() == null
                || damage.recipient() != DamageRecipient.TARGET_PLAYER) {
            return false;
        }

        Permanent sourcePermanent = match.permanent();
        Card sourceCard = sourcePermanent.getCard();
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(damage)), true,
                damage.triggeredTargetFilter(), dc.damageDealt(), sourcePermanent.getId(),
                new Permanent(sourcePermanent)));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} triggers after paired permanent was dealt damage",
                match.gameData().id, sourceCard.getName());
        return true;
    }

    @CollectsCounterTrigger(value = DealDamageToAnyTargetEffect.class,
            watch = CounterWatch.ON_YOU_PUT_COUNTERS_ON_PERMANENT_OR_PLAYER)
    private boolean handleCountersPlaced(TriggerMatchContext match,
            DealDamageToAnyTargetEffect trigger, TriggerContext ctx) {
        TriggerContext.CountersPlaced countersPlaced = (TriggerContext.CountersPlaced) ctx;
        if (countersPlaced.amount() <= 0) return false;

        // "That much damage" (All Will Be One) is the entry's event value: the counters put, carried by the choice.
        Card sourceCard = match.permanent().getCard();
        match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                sourceCard, match.controllerId(), new ArrayList<>(List.of(trigger)), false,
                sourceCard.getTargetFilter(), countersPlaced.amount(), match.permanent().getId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sourceCard));
        log.info("Game {} - {} counter-placement trigger fires for {} damage",
                match.gameData().id, sourceCard.getName(), countersPlaced.amount());
        return true;
    }

    @CollectsDamageTrigger(value = MayEffect.class, watch = DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE)
    private boolean handleAnyCreatureDealtDamageMay(TriggerMatchContext match, MayEffect effect,
                                                     TriggerContext ctx) {
        TriggerContext.AnyCreatureDealtDamage dc = (TriggerContext.AnyCreatureDealtDamage) ctx;
        if (dc.damageDealt() <= 0) return false;

        return enqueueAnyCreatureDealtDamage(match, effect, dc);
    }

    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ANY_CREATURE_DEALT_DAMAGE)
    private boolean handleAnyCreatureDealtDamageDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.AnyCreatureDealtDamage dc = (TriggerContext.AnyCreatureDealtDamage) ctx;
        if (dc.damageDealt() <= 0) return false;

        return enqueueAnyCreatureDealtDamage(match, effect, dc);
    }

    private boolean enqueueAnyCreatureDealtDamage(TriggerMatchContext match, CardEffect effect,
                                                   TriggerContext.AnyCreatureDealtDamage dc) {
        GameData gameData = match.gameData();
        // "Do this only once each turn" (CR 603.2h): taking the action marks the source.
        boolean markOnAcceptance = match.markSourceOncePerTurnOnAcceptance();
        CardEffect unwrapped = effect;
        if (unwrapped instanceof MayEffect may
                && (may.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || may.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            // The target of an optional targeted trigger is chosen as the ability goes on the stack (CR 603.3d);
            // only the choice to use the option waits until resolution (CR 603.5). The target choice carries the
            // "do this only once each turn" mark to the stack entry it makes.
            Permanent sourcePermanent = match.permanent();
            Card sourceCard = sourcePermanent.getCard();
            var choice = new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)), false,
                    targetFilterForTriggeredEffect(sourceCard, may), dc.damageDealt(),
                    sourcePermanent.getId(), new Permanent(sourcePermanent));
            gameData.queueInteraction(markOnAcceptance ? choice.withMarkSourceOncePerTurnOnAcceptance() : choice);
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            log.info("Game {} - {} targeted optional trigger for damage dealt to a creature",
                    gameData.id, sourceCard.getName());
            return true;
        }
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                match.permanent().getCard(),
                match.controllerId(),
                match.permanent().getCard().getName() + "'s ability",
                new ArrayList<>(List.of(unwrapped)),
                null,
                match.permanent().getId()
        );
        entry.setMarkSourceOncePerTurnOnAcceptance(markOnAcceptance);
        entry.setTargetId(dc.damagedCreature().getId());
        entry.setEventValue(dc.damageDealt());
        entry.setTriggeringPermanentId(dc.damagedCreature().getId());
        entry.setTriggeringPermanentControllerId(dc.damagedCreatureControllerId());
        entry.setNonTargeting(true);
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.abilityTriggers(match.permanent().getCard()));
        log.info("Game {} - {} triggers for damage dealt to a creature", gameData.id,
                match.permanent().getCard().getName());
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
    private boolean handleEnchantedCreatureDealtDamageDefault(TriggerMatchContext match, CardEffect effect,
                                                               TriggerContext ctx) {
        TriggerContext.DamageToCreature dc = (TriggerContext.DamageToCreature) ctx;
        if (dc.damageDealt() <= 0) return false;

        GameData gameData = match.gameData();
        Permanent aura = match.permanent();
        Permanent creature = dc.damagedCreature();
        Permanent lastKnown = lastKnownDamageSource(gameData, creature);
        int power = gameQueryService.getEffectivePower(gameData, creature);
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    aura.getCard(), match.controllerId(), new ArrayList<>(List.of(effect)), false,
                    targetFilterForTriggeredEffect(aura.getCard(), effect), 0, aura.getId(), new Permanent(aura),
                    false, creature.getId(), null, match.controllerId(), null, null, false, null, null, power,
                    false, lastKnown).withEventValue(dc.damageDealt()));
        } else {
            StackEntry entry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    aura.getCard(),
                    match.controllerId(),
                    aura.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(effect)),
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
    private boolean handleControllerDealtDamageByAllySource(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();

        if (effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    perm.getCard(), match.controllerId(), new ArrayList<>(List.of(effect)), true,
                    targetFilterForTriggeredEffect(perm.getCard(), effect), dc.amount(), perm.getId()));
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
                new ArrayList<>(List.of(effect)),
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
    private boolean handleControllerDamagePreventedDefault(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        GameData gameData = match.gameData();
        Permanent perm = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                perm.getCard(), match.controllerId(), perm.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)), null, perm.getId());
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
    private boolean handlePlayerDamageDefault(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        PlayerDamage damage = PlayerDamage.of(ctx);
        return queuePlayerDamageAbility(match, effect, damage, damage.damagedPlayerId());
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
    private boolean handleOpponentSourceDamageDefault(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
        PlayerDamage damage = PlayerDamage.of(ctx);
        return queuePlayerDamageAbility(match, effect, damage, damage.sourceControllerId());
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
    private boolean queuePlayerDamageAbility(TriggerMatchContext match, CardEffect effect, PlayerDamage damage,
            UUID thatPlayerId) {
        GameData gameData = match.gameData();
        Permanent watcher = match.permanent();
        Card sourceCard = watcher != null ? watcher.getCard() : match.sourceCard();
        if (sourceCard == null) return false;
        UUID sourcePermanentId = watcher == null ? null : watcher.getId();
        boolean thatPlayer = isThatPlayer(sourceCard, effect);
        if (!thatPlayer && (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER))) {
            gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                    sourceCard, match.controllerId(), List.of(effect), sourcePermanentId, match.controllerId(),
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
                new ArrayList<>(List.of(effect)),
                null,
                sourcePermanentId);
        if (thatPlayer || effect.targetSpec().declaredTarget() == null) {
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
    private boolean handleAllyCreaturesDealDamageToPlayer(TriggerMatchContext match, CardEffect effect,
            TriggerContext ctx) {
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
                new ArrayList<>(List.of(effect)),
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
    private boolean handleEnchantedPlayerDealtDamage(TriggerMatchContext match,
            CardEffect effect, TriggerContext ctx) {
        TriggerContext.DamageToControllerAmount dc = (TriggerContext.DamageToControllerAmount) ctx;
        if (dc.amount() <= 0) return false;

        Permanent aura = match.permanent();
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                aura.getCard(),
                match.controllerId(),
                aura.getCard().getName() + "'s ability",
                new ArrayList<>(List.of(effect)),
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
    private boolean handleSelfDealsDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsDamage sd = (TriggerContext.SourceDealsDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        GameData gameData = match.gameData();
        Card sourceCard = sd.sourceCard();
        // The source may have died dealing the damage; keep its last-known permanent id when present.
        UUID sourcePermanentId = sd.sourcePermanentId();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            gameData.queueInteraction(new PermanentChoiceContext.SelfTriggeredAbilityTarget(
                    sourceCard, match.controllerId(), new ArrayList<>(List.of(effect)),
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

    /**
     * Blaze Commando: "Whenever an instant or sorcery spell you control deals damage, ...". The
     * source/controller gating is done by the dispatcher; here the watcher permanent simply queues
     * its ability once for the batched damage event.
     */
    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE)
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
    private boolean handleSelfDealsCombatDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        Card sourceCard = sd.sourceCard();
        if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            int minTargets = effect instanceof ReturnCardFromGraveyardEffect returnEffect
                    && !returnEffect.upTo() ? 1 : 0;
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    sourceCard, sd.sourceControllerId(), new ArrayList<>(List.of(effect)), null, minTargets, 0,
                    match.permanent() != null && match.permanent().isAlternateCost()));
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
                    sd.sourcePermanentId(), hasOptionalTarget(sourceCard, effect)));
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

    @CollectsCombatTrigger(value = ChooseOneAtTriggerTimeEffect.class,
            watch = CombatWatch.ON_SELF_DEALS_COMBAT_DAMAGE_TO_PLAYER_OR_BATTLE)
    private boolean handleSelfDealsCombatDamageModalAtTriggerTime(TriggerMatchContext match,
            ChooseOneAtTriggerTimeEffect effect, TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        if (sd.totalDamage() <= 0) return false;

        match.gameData().queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                sd.sourceCard(), sd.sourceControllerId(), effect.choice(), sd.sourcePermanentId()));
        gameLogService.append(match.gameData(), GameLog.abilityTriggers(sd.sourceCard()));
        log.info("Game {} - {} ON_SELF_DEALS_COMBAT_DAMAGE trigger chooses modes as it is put on the stack",
                match.gameData().id, sd.sourceCard().getName());
        return true;
    }

    @CollectsCombatTrigger(value = CardEffect.class, watch = CombatWatch.ON_ALLY_CREATURE_DEALS_COMBAT_DAMAGE)
    private boolean handleAllyCreatureDealsCombatDamage(TriggerMatchContext match, CardEffect effect, TriggerContext ctx) {
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
                new ArrayList<>(List.of(effect)),
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
    private boolean handleEquippedCreatureDealsCombatDamage(TriggerMatchContext match, CardEffect effect,
                                                              TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        return queueEquippedCreatureCombatDamageTrigger(match, effect, sd, sd.totalDamage());
    }

    /**
     * "Whenever equipped creature deals combat damage to a player, ... that much" counts only the
     * damage dealt to players, not what the same creature assigned to blockers.
     */
    @CollectsCombatTrigger(value = CardEffect.class, watch = CombatWatch.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER)
    private boolean handleEquippedCreatureDealsCombatDamageToPlayer(TriggerMatchContext match, CardEffect effect,
                                                                      TriggerContext ctx) {
        TriggerContext.SourceDealsCombatDamage sd = (TriggerContext.SourceDealsCombatDamage) ctx;
        return queueEquippedCreatureCombatDamageTrigger(match, effect, sd, sd.damageToPlayers());
    }

    private boolean queueEquippedCreatureCombatDamageTrigger(TriggerMatchContext match, CardEffect effect,
                                                               TriggerContext.SourceDealsCombatDamage sd,
                                                               int damage) {
        if (sd.totalDamage() <= 0 || match.permanent() == null) return false;
        if (effect instanceof ConjureDuplicateOfCombatDamageSourceIntoHandEffect && sd.sourceCard().isToken()) return false;

        Permanent equipment = match.permanent();
        if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)) {
            boolean optionalTarget = equipment.getCard().getTargetGroups().size() == 1
                    && equipment.getCard().getTargetGroups().getFirst().getMinTargets() == 0
                    && equipment.getCard().getTargetGroups().getFirst().getMaxTargets() == 1
                    && equipment.getCard().getEffectTargetIndex(effect)
                    == equipment.getCard().getTargetGroups().getFirst().getIndex();
            match.gameData().queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                    equipment.getCard(), match.controllerId(), new ArrayList<>(List.of(effect)),
                    !effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT),
                    targetFilterForTriggeredEffect(equipment.getCard(), effect), damage, equipment.getId(),
                    optionalTarget));
            gameLogService.append(match.gameData(), GameLog.abilityTriggers(equipment.getCard()));
            log.info("Game {} - {} ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE trigger awaits target",
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
     * "Whenever this creature is dealt damage, you may destroy target nonland permanent"
     * (High Priest of Penance). CR 603.3d: a targeted "may" trigger chooses its target as the
     * ability goes on the stack, while the "you may" is answered on resolution — so the target
     * choice is queued here, honouring the card's own target filter. Non-targeting mays keep the
     * plain trigger entry, whose targetId is the damaged permanent itself.
     */
    @CollectsDamageTrigger(value = MayEffect.class, watch = DamageWatch.ON_DEALT_DAMAGE)
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

    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_DEALT_DAMAGE)
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
                    dc.damageDealt(), damagedCreature.getId(),
                    hasOptionalTarget(damagedCreature.getCard(), effect)));
            gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
            log.info("Game {} - {} ON_DEALT_DAMAGE targeted trigger fires",
                    gameData.id, damagedCreature.getCard().getName());
            return true;
        }
        addDealtDamageEntry(match.gameData(), dc, effect);
        return true;
    }

    @CollectsDamageTrigger(value = CardEffect.class, watch = DamageWatch.ON_NONCOMBAT_DAMAGE_TO_SELF)
    private boolean handleNoncombatDamageToSelfDefault(TriggerMatchContext match, CardEffect effect,
                                                       TriggerContext ctx) {
        return handleDealtDamageDefault(match, effect, ctx);
    }

    private TargetFilter targetFilterForTriggeredEffect(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        if (targetIndex >= 0 && targetIndex < card.getTargetGroups().size()) {
            return card.getTargetGroups().get(targetIndex).getFilter();
        }
        if (effect instanceof DamageDealingEffect damageEffect
                && damageEffect.triggeredTargetFilter() != null) {
            return damageEffect.triggeredTargetFilter();
        }
        return card.getTargetFilter();
    }

    private boolean hasOptionalTarget(Card card, CardEffect effect) {
        int targetIndex = card.getEffectTargetIndex(effect);
        return targetIndex >= 0 && targetIndex < card.getTargetGroups().size()
                && card.getTargetGroups().get(targetIndex).getMinTargets() == 0;
    }

    /**
     * Queues a plain ON_DEALT_DAMAGE triggered ability. The damage dealt is snapshotted onto the
     * entry's eventValue so "it deals that much damage" effects can read it with an
     * {@code EventValue} amount (Stuffy Doll), and the damage source is its triggering permanent and controller
     * ("that source's controller").
     */
    private void addDealtDamageEntry(com.github.laxika.magicalvibes.model.GameData gameData,
            TriggerContext.DamageToCreature dc, CardEffect effect) {
        Permanent damagedCreature = dc.damagedCreature();
        int damageDealt = dc.damageDealt();
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
        // "That source" and "that source's controller": this damage has one source, the permanent it was (if any) and
        // its controller as it dealt the damage.
        triggerEntry.setTriggeringPermanentId(dc.sourcePermanentId());
        triggerEntry.setTriggeringPermanentControllerId(dc.damageSourceControllerId());
        triggerEntry.setSourcePermanentSnapshot(lastKnownDamageSource(gameData, damagedCreature));
        gameData.stack.add(triggerEntry);
        gameLogService.append(gameData, GameLog.abilityTriggers(damagedCreature.getCard()));
        log.info("Game {} - {} ON_DEALT_DAMAGE trigger fires", gameData.id, damagedCreature.getCard().getName());
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

    private TargetFilter targetFilterFor(Permanent source, CardEffect effect) {
        int targetIndex = source.getCard().getEffectTargetIndex(effect);
        if (targetIndex < 0 || targetIndex >= source.getCard().getTargetGroups().size()) {
            return null;
        }
        return source.getCard().getTargetGroups().get(targetIndex).getFilter();
    }
}
