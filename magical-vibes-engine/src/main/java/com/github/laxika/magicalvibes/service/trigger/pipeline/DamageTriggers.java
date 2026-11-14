package com.github.laxika.magicalvibes.service.trigger.pipeline;

import com.github.laxika.magicalvibes.model.rules.DamageDealtEvent;

import com.github.laxika.magicalvibes.model.trigger.CombatTrigger;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealsDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamage;
import com.github.laxika.magicalvibes.model.action.DelayedWatchedCreatureDealtDamageByAttackingCreature;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.trigger.DamageTrigger;
import com.github.laxika.magicalvibes.model.trigger.Trigger;
import com.github.laxika.magicalvibes.model.trigger.TriggerLimit;
import com.github.laxika.magicalvibes.model.trigger.TriggerMultiplicity;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.GrantedTriggeredAbilitySupport;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.trigger.TriggerContext;
import com.github.laxika.magicalvibes.service.trigger.WatchedAbilities;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

/**
 * Triggers the damage abilities (a {@code DamageTrigger}: damage is dealt, CR 120.1, 120.2, or damage to you is
 * prevented, CR 615.1) and the combat-damage abilities (a {@code CombatTrigger} on combat damage, CR 510.2) that a damage
 * rules event names, each handed whole to the stack door with the record of the damage.
 */
@Slf4j
@Service
public class DamageTriggers {

    /** A record that names nothing beyond what the request's own fields name. */
    private static final TriggerRecord NO_EXTRA_RECORD = entry -> {
    };

    /**
     * One effect of a damage or combat-damage ability as its collector reads it for the trigger condition watched as
     * {@code watched}: the effect wrapped in the ability's intervening "if" (CR 603.4, judged again as it resolves),
     * the ability, and the filter of the condition on the permanent ({@link TriggerMatcher#damageCondition}: the
     * damaged permanent for an "is dealt damage" event and for "this deals damage", the dealer for the other events).
     */
    record Read(CardEffect effect, TriggeredAbility ability, PermanentPredicate filter,
                boolean simultaneousWithPrevious) {

        TriggerOptions options() {
            return ability.options();
        }

        TriggerLimit limit() {
            return ability.options().limit();
        }

        TriggerMultiplicity multiplicity() {
            return ability.options().multiplicity();
        }

        boolean oneOrMore() {
            return multiplicity() == TriggerMultiplicity.ONE_OR_MORE;
        }

        boolean eachBlocker() {
            return multiplicity() == TriggerMultiplicity.EACH_BLOCKER;
        }

        /** "Whenever a source deals damage to this": once for each source in one damage event (CR 603.2c). */
        boolean eachSource() {
            return multiplicity() == TriggerMultiplicity.EACH_SOURCE;
        }

        /** Marks the ability's limit as used by {@code sourceId} (CR 603.2h). */
        void markLimit(GameData gameData, UUID sourceId) {
            TriggeredAbilityStackService.markLimit(gameData, limit(), sourceId, ability.limitKey());
        }

        /** Throws unless the ability has no limit and triggers once for each event: its collector reads neither. */
        void requireNoLimitOrMultiplicity(Trigger watched) {
            requireNoLimitOrMultiplicity(this, watched);
        }

        /** The first effect of {@code ability} as read for {@code watched}. */
        static Read of(TriggeredAbility ability, Trigger watched) {
            return read(ability, watched).getFirst();
        }

        static List<Read> read(TriggeredAbility ability, Trigger watched) {
            PermanentPredicate filter = filter(ability, watched);
            Condition interveningIf = ability.options().interveningIf();
            List<Read> reads = new ArrayList<>(ability.effects().size());
            for (EffectRegistration registration : ability.effects()) {
                CardEffect effect = interveningIf == null ? registration.effect()
                        : new ConditionalEffect(interveningIf, registration.effect());
                reads.add(new Read(effect, ability, filter, registration.simultaneousWithPrevious()));
            }
            return reads;
        }

        static List<Read> read(List<TriggeredAbility> abilities, Trigger watched) {
            List<Read> reads = new ArrayList<>();
            for (TriggeredAbility ability : abilities) {
                reads.addAll(read(ability, watched));
            }
            return reads;
        }

        /** The reads of {@code abilities}, one list for each ability: one object on the stack (CR 603.3). */
        static List<List<Read>> readByAbility(List<TriggeredAbility> abilities, Trigger watched) {
            List<List<Read>> byAbility = new ArrayList<>(abilities.size());
            for (TriggeredAbility ability : abilities) {
                List<Read> reads = read(ability, watched);
                if (!reads.isEmpty()) {
                    byAbility.add(reads);
                }
            }
            return byAbility;
        }

        /** The abilities of {@code card} with a condition watched as {@code watched} on the battlefield. */
        static List<TriggeredAbility> printed(Card card, Trigger watched) {
            return WatchedAbilities.printed(card, watched, Zone.BATTLEFIELD);
        }

        /**
         * The effects of {@code abilities} for {@code watched}, for a collector that reads no filter, limit or
         * multiplicity; this throws on one.
         */
        static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched) {
            return effects(abilities, watched, filter -> {
                throw new IllegalStateException("The collector of " + watched + " judges no filter: " + filter);
            });
        }

        /**
         * The effects of the abilities of {@code abilities} whose filter for {@code watched} {@code filterMatches}
         * accepts (an ability without one always triggers), for a collector that reads no limit or multiplicity.
         */
        static List<CardEffect> effects(List<TriggeredAbility> abilities, Trigger watched,
                                        java.util.function.Predicate<PermanentPredicate> filterMatches) {
            List<CardEffect> effects = new ArrayList<>();
            for (Read read : read(abilities, watched)) {
                requireNoLimitOrMultiplicity(read, watched);
                if (read.filter() == null || filterMatches.test(read.filter())) {
                    effects.add(read.effect());
                }
            }
            return effects;
        }

        static List<CardEffect> effectsOf(List<Read> reads) {
            return reads.stream().map(Read::effect).toList();
        }

        static void requireNoLimitOrMultiplicity(Read read, Trigger watched) {
            if (read.limit() != TriggerLimit.NONE || read.multiplicity() != TriggerMultiplicity.EACH) {
                throw new IllegalStateException("The collector of " + watched + " reads no " + read.options() + ": "
                        + read.ability());
            }
        }

        /** The filter on the permanent of {@code ability}'s condition watched as {@code watched}, or {@code null}. */
        static PermanentPredicate filter(TriggeredAbility ability, Trigger watched) {
            DamageTrigger damage = TriggerMatcher.damageCondition(ability, watched, null);
            if (damage != null) {
                return damage.filter();
            }
            CombatTrigger combat = watched instanceof CombatTrigger combatWatched
                    ? TriggerMatcher.combatCondition(ability, combatWatched, null) : null;
            return combat == null || combat.equals(watched) ? null : combat.filter();
        }

        /** {@code ability}'s damage condition watched as {@code watched}, with its filters, or {@code null}. */
        static DamageTrigger trigger(TriggeredAbility ability, Trigger watched) {
            return TriggerMatcher.damageCondition(ability, watched, null);
        }
    }

    @Autowired @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;
    @Autowired @Lazy
    private GameQueryService gameQueryService;
    @Autowired @Lazy
    private PredicateEvaluationService predicateEvaluationService;
    @Autowired @Lazy
    private ConditionEvaluationService conditionEvaluationService;
    @Autowired @Lazy
    private GameLogService gameLogService;
    @Autowired @Lazy
    private GrantedTriggeredAbilitySupport grantedTriggeredAbilitySupport;
    @Autowired @Lazy
    private TargetLegalityService targetLegalityService;
    @Autowired @Lazy
    private TriggerIndex triggerIndex;
    @Autowired @Lazy
    private TriggerCollectionService triggerCollectionService;

    /**
     * Triggers the damage abilities {@code event} triggers (CR 603.2), each handed whole to the stack door: for each
     * kind of damage event, the abilities that see it in the order the class's collectors list them.
     */
    public void trigger(GameData gameData, DamageDealtEvent event) {
        switch (event) {
            case DamageDealtEvent.ToPermanent dealt -> toPermanent(gameData, dealt);
            case DamageDealtEvent.ToPlayer dealt -> toPlayer(gameData, dealt);
            case DamageDealtEvent.SourcesDealtDamage dealt -> sourcesDealtDamage(gameData, dealt);
            case DamageDealtEvent.DealtByCreature dealt ->
                    queueEnchantedCreatureDealsDamageTriggers(gameData, dealt.creature(), dealt.damage());
            case DamageDealtEvent.CombatToPermanent dealt ->
                    checkAnyPermanentDealtDamageTriggers(gameData, dealt.permanent(), dealt.damage());
            case DamageDealtEvent.CombatToPlayer dealt -> {
                checkEnchantedPlayerDealtDamageTriggers(gameData, dealt.playerId(), dealt.damage());
                dealt.damageBySource().forEach((sourceId, damage) ->
                        checkOpponentDealtDamageTriggers(gameData, dealt.playerId(), sourceId, damage));
            }
            case DamageDealtEvent.CombatToCreature dealt -> {
                checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(gameData, dealt.source(),
                        dealt.target(), dealt.damage());
                checkEnchantedCreatureDealtDamageTriggers(gameData, dealt.target(), dealt.damage());
            }
            case DamageDealtEvent.PreventedToPlayer prevented ->
                    checkControllerDamagePreventedTriggers(gameData, prevented.playerId(), prevented.amount());
        }
    }

    /**
     * Damage a spell or ability dealt to a permanent: "whenever a permanent is dealt damage", the delayed abilities
     * watching the creature, "whenever this is dealt (noncombat) damage", "whenever a source you control deals
     * noncombat damage to a creature", the planeswalker abilities, the abilities that see a creature dealt damage (only
     * a creature: CR 603.2, the layers deciding what is one, CR 613.1d), "deals damage to you or a permanent you
     * control", then the excess-damage abilities.
     */
    private void toPermanent(GameData gameData, DamageDealtEvent.ToPermanent dealt) {
        Permanent target = dealt.target();
        int damage = dealt.damage();
        UUID sourceControllerId = dealt.sourceControllerId();
        Permanent reflectionSource = dealt.reflectionSource();
        UUID damagedCreatureControllerId = dealt.damagedCreatureControllerId();
        checkAnyPermanentDealtDamageTriggers(gameData, target, damage);
        checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(gameData, dealt.dealer(), target, damage);
        checkDealtDamageToCreatureTriggers(gameData, target, damage, sourceControllerId, dealt.sourceCard(),
                dealt.sourceId());
        checkNoncombatDamageToSelfTriggers(gameData, target, damage, sourceControllerId, dealt.sourceCard(),
                dealt.sourceId());
        checkAllySourceDealtNoncombatDamageToCreatureTriggers(gameData, sourceControllerId, target, damage);
        if (gameQueryService.isPlaneswalker(gameData, target)) {
            checkAllyDealtDamageToPlaneswalkerTriggers(gameData, reflectionSource, sourceControllerId,
                    target.getId(), damage, false);
        }
        if (gameQueryService.isCreature(gameData, target)) {
            if (damagedCreatureControllerId != null) {
                checkOpponentCreatureDealtDamageTriggers(gameData, damagedCreatureControllerId);
                checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(gameData, target,
                        damagedCreatureControllerId, damage);
            }
            checkAnyCreatureDealtDamageTriggers(gameData, target, damage);
            queueEnchantedCreatureDealsDamageToCreatureTriggers(gameData, reflectionSource, target.getId(), damage);
            checkAllyDealtDamageToCreatureTriggers(gameData, reflectionSource, sourceControllerId,
                    damagedCreatureControllerId, target.getId(), target, damage, false);
        }
        // "Whenever a creature deals damage to you or a permanent you control" sees any damaged permanent; its
        // damaged-permanent filter narrows it.
        checkCreatureDamageToYouOrYourPermanentTriggers(gameData, damagedCreatureControllerId, target,
                reflectionSource, damage);
        if (dealt.excessDamage() > 0) {
            checkOpponentPermanentDealtExcessDamageTriggers(gameData, target, dealt.targetControllerId(),
                    dealt.excessDamage(), dealt.noncombat());
            checkOpponentCreatureDealtExcessNoncombatDamageTriggers(gameData, dealt.entry(), target,
                    dealt.targetControllerId(), dealt.excessDamage());
        }
    }

    /**
     * Damage a spell or ability dealt to a player: the abilities of the Auras and Equipment of the creature that dealt
     * it, then those that see the player dealt damage, in the order below; the stack object's controller is the damage
     * source's (its caster or activator).
     */
    private void toPlayer(GameData gameData, DamageDealtEvent.ToPlayer dealt) {
        UUID playerId = dealt.playerId();
        int damage = dealt.damage();
        StackEntry entry = dealt.entry();
        UUID sourceControllerId = dealt.sourceControllerId();
        queueEnchantedCreatureDealsDamageTriggers(gameData, dealt.dealtByCreature(), damage);
        checkEnchantedPlayerDealtDamageTriggers(gameData, playerId, damage);
        checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
        checkEnchantedCreatureDealtDamageToControllerReflectTriggers(gameData, playerId,
                entry.getSourcePermanentId(), damage);
        checkControllerDealtDamageTriggers(gameData, playerId, entry.getControllerId(), damage);
        checkAllySourceDealtDamageToOpponentTriggers(gameData, playerId, entry.getControllerId(),
                entry.getSourcePermanentId(), damage);
        if (dealt.creatureSource() != null) {
            checkAllyCreaturesDealDamageToPlayerTriggers(gameData, sourceControllerId, playerId,
                    List.of(dealt.creatureSource()));
        }
        if (dealt.opponentDealer() != null) {
            checkAllyCreaturesDealDamageToOpponentTriggers(gameData, sourceControllerId, playerId,
                    List.of(dealt.opponentDealer()));
        }
        checkAllySourceDealtNoncombatDamageToOpponentTriggers(gameData, playerId, sourceControllerId, damage);
        checkOpponentDealtDamageTriggers(gameData, playerId, entry.getSourcePermanentId(), damage);
        checkCreatureDamageToYouOrYourPermanentTriggers(gameData, playerId, null, dealt.sourcePermanent(), damage);
        if (dealt.dealingPermanent() != null) {
            checkSourceDealsDamageToPlayerTriggers(gameData, dealt.dealingPermanent(), dealt.dealingControllerId(),
                    playerId, damage);
        }
        checkNoncombatDamageToOpponentTriggers(gameData, playerId, sourceControllerId, damage);
        checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(gameData, playerId, entry);
    }

    /**
     * A resolution's noncombat damage, each source's added up (CR 603.2c): "whenever an instant or sorcery spell you
     * control deals damage", "whenever a source deals damage", "whenever a source an opponent controls deals damage to
     * you or a permanent you control", "whenever an instant or sorcery spell you control deals damage to an opponent or
     * a battle", then "whenever damage is dealt to a player" once for each player and damage event.
     */
    private void sourcesDealtDamage(GameData gameData, DamageDealtEvent.SourcesDealtDamage dealt) {
        for (Map.Entry<UUID, PendingSourceDamage> event : dealt.sources().entrySet()) {
            PendingSourceDamage batch = event.getValue();
            if (!event.getKey().equals(batch.getSourceCard().getId())) {
                checkAllyInstantOrSorcerySpellDealsDamageTriggers(gameData, batch);
            }
            queueSourceDealsDamageReflections(gameData,
                    batch.getSourceCard(), batch.getControllerId(), batch.getSourcePermanentId(), batch.getAmount(),
                    batch.getDamageToPlayers(), batch.getSelfDealsDamageAbilities(),
                    batch.getSingleCreatureSpellTargetId(), batch.getDamageToPermanents(), false,
                    batch.getDamagedPermanentSnapshots());
            List<SourceDamageRecipient> damageRecipients = batch.getDamageRecipients().stream()
                    .map(recipient -> new SourceDamageRecipient(recipient.playerId(), recipient.permanentId()))
                    .toList();
            checkOpponentSourceDamageToYouOrYourPermanentTriggers(gameData, batch.getSourceCard(),
                    batch.getControllerId(), batch.getSourcePermanentId(), damageRecipients);
            for (int i = 0; i < batch.getInstantOrSorceryDamageRecipientCount(); i++) {
                checkInstantOrSorceryDamageToOpponentOrBattleTriggers(gameData, batch.getSourceCard(),
                        batch.getControllerId(), true);
            }
        }
        damageToPlayersByEvent(dealt.sources().values()).forEach(damageToPlayers -> damageToPlayers.forEach(
                (playerId, damage) -> checkPlayerDealtDamageTriggers(gameData, playerId, damage)));
    }

    /**
     * The damage each player was dealt in each damage event of {@code batches}, every source's summed: the batches
     * with the same resolution damage event are one event (CR 603.2c: "whenever damage is dealt to a player" triggers
     * once for it), and a batch dealt outside one is an event of its own.
     */
    private static List<Map<UUID, Integer>> damageToPlayersByEvent(Collection<PendingSourceDamage> batches) {
        Map<Object, Map<UUID, Integer>> byEvent = new LinkedHashMap<>();
        for (PendingSourceDamage batch : batches) {
            Object event = batch.getDamageEvent() != null ? batch.getDamageEvent() : batch;
            Map<UUID, Integer> damageToPlayers = byEvent.computeIfAbsent(event, ignored -> new LinkedHashMap<>());
            batch.getDamageToPlayers().forEach((playerId, damage) ->
                    damageToPlayers.merge(playerId, damage, Integer::sum));
        }
        return new ArrayList<>(byEvent.values());
    }

    /**
     * The "whenever this deals damage" abilities {@code sourcePermanent} has as it deals noncombat damage, kept with
     * the damage until its resolution's damage is added up: its card's unless it is face down or lost them (CR 708.2),
     * those granted it, and those other permanents' static abilities grant it.
     */
    public List<TriggeredAbility> selfDealsDamageAbilities(GameData gameData, Card sourceCard,
                                                           Permanent sourcePermanent) {
        return abilities(gameData, sourcePermanent, Triggers.dealsDamage());
    }

    /**
     * The abilities with the combat trigger {@code watch} of {@code permanent}, one list of reads for each ability (one
     * object on the stack, CR 603.3): its printed ones when {@code printed}, then those other permanents' static
     * abilities grant it.
     */
    private List<List<Read>> combatReads(GameData gameData, Permanent permanent, Trigger watch) {
        return Read.readByAbility(abilities(gameData, permanent, watch), watch);
    }

    /**
     * The abilities that {@code watch} reads of {@code permanent} as their collector reads them, one list of reads for
     * each ability (one object on the stack, CR 603.3): its printed ones, those granted until end of turn, combat or
     * next upkeep, those until it leaves the battlefield, and those other permanents' static abilities grant it, in
     * that order.
     */
    private List<List<Read>> combatReadsWithGrants(GameData gameData, Permanent permanent, Trigger watch) {
        return Read.readByAbility(abilities(gameData, permanent, watch), watch);
    }

    /** {@link GrantedTriggeredAbilitySupport#grantedWatchedAbilities(GameData, Permanent, Trigger)}. */
    public List<TriggeredAbility> grantedWatchedAbilities(GameData gameData, Permanent permanent, Trigger watched) {
        return grantedTriggeredAbilitySupport.grantedWatchedAbilities(gameData, permanent, watched);
    }

    private static void requireNoLimit(TriggeredAbility ability, Card card) {
        if (ability.options().limit() != TriggerLimit.NONE) {
            throw new IllegalStateException(card.getName() + "'s triggered ability has a limit its reader doesn't "
                    + "judge: " + ability.options());
        }
    }

    /**
     * Fires turn-scoped global watchers registered for {@code ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER}
     * (e.g. The Sea Devils chapter III). The watcher's trigger source is the dealing creature, so
     * effects that "it deals damage" use that creature, and the damaged player is bound as the
     * trigger's target. The trigger's filter is judged on the dealer.
     */
    private void enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                         UUID creatureControllerId,
                                                                         UUID damagedPlayerId, int damageDealt) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(Triggers.creatureDealsCombatDamageToPlayer())
                    || watcher.watchedPermanentId() != null) {
                continue;
            }
            Trigger anyCombatDamage = Triggers.creatureDealsCombatDamageToPlayer();
            List<Read> reads = Read.read(watcher.ability(), anyCombatDamage);
            Read read = reads.getFirst();
            Read.requireNoLimitOrMultiplicity(read, anyCombatDamage);
            if (read.filter() != null
                    && !predicateEvaluationService.matchesPermanentPredicate(creature, read.filter(),
                    FilterContext.of(gameData)
                            .withSourceCardId(watcher.sourceCard().getId())
                            .withSourceControllerId(watcher.controllerId()))) {
                continue;
            }
            List<CardEffect> resolvedEffects = Read.effectsOf(reads);
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s triggered ability", resolvedEffects, damagedPlayerId,
                    creature.getId());
            entry.setTriggeringPermanentId(creature.getId());
            entry.setTriggeringPermanentControllerId(creatureControllerId);
            entry.setEventValue(damageDealt);
            entry.setNonTargeting(true);
            // "That player" is the player dealt damage, recorded whatever the ability chooses.
            entry.setTriggeringPlayerId(damagedPlayerId);
            entry.setAttackedTargetId(damagedPlayerId);
            if (resolvedEffects.stream().anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))) {
                // The target is chosen as the trigger is put on the stack (CR 603.3d), by the watcher's
                // controller; "that much" is the damage dealt.
                triggeredAbilityStackService.putMade(gameData, entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
                continue;
            }
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary global ON_ANY_CREATURE_COMBAT_DAMAGE_TO_PLAYER trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    public void checkAnyCreatureCombatDamageToOpponentTriggers(GameData gameData, Permanent creature,
                                                               UUID creatureControllerId,
                                                               UUID damagedPlayerId, int damageDealt,
                                                               Set<String> firedOncePerStepAbilities) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        enqueueTemporaryAnyCreatureCombatDamageToPlayerTriggers(gameData, creature, creatureControllerId,
                damagedPlayerId, damageDealt);

        // The creature dealing combat damage to a player is the event the "triggers an additional time" effects judge.
        TriggerContext event = new TriggerContext.SourceDealsCombatDamage(creature.getCard(), creatureControllerId,
                creature.getId(), damageDealt, damageDealt);
        gameData.forEachPermanent((controllerId, source) -> {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            // Each ability, of either watch, is handed whole to the stack door (CR 603.3); its filter, conditions and
            // "one or more" are judged once, for the ability.
            List<List<Read>> toOpponent = controllerId.equals(damagedPlayerId)
                    ? List.of()
                    : combatReads(gameData, source, Triggers.creatureDealsCombatDamageToOpponent());
            List<List<Read>> abilities = new ArrayList<>(toOpponent);
            abilities.addAll(combatReads(gameData, source, Triggers.creatureDealsCombatDamageToPlayer()));
            FilterContext dealerContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                Read read = abilities.get(abilityIndex).getFirst();
                // The ability's place among the source's abilities of its watch, the same whichever player was dealt
                // the damage.
                String abilityKey = source.getId() + (abilityIndex < toOpponent.size()
                        ? ":opponent:" + abilityIndex : ":player:" + (abilityIndex - toOpponent.size()));
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The \"a creature deals combat damage to a player\" collector "
                            + "reads no " + read.options() + ": " + read.ability());
                }
                // "Whenever a [filter] creature deals combat damage to one of your opponents": the trigger's filter.
                if (read.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(creature, read.filter(), dealerContext)) {
                    continue;
                }
                // "Whenever your opponents are dealt combat damage, if any of that damage was dealt by a [filter]":
                // once for the combat damage step, however many creatures dealt it to however many opponents; each
                // such ability of the source on its own (CR 603.3b).
                if (read.oneOrMore() && firedOncePerStepAbilities.contains(abilityKey)) {
                    continue;
                }
                boolean triggered = putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                        ConditionContext.forPermanent(source, controllerId).withTargetId(damagedPlayerId), event,
                        entry -> {
                            entry.setTargetId(damagedPlayerId);
                            entry.setNonTargeting(true);
                            if (!read.oneOrMore()) {
                                entry.setTriggeringPermanentId(creature.getId());
                                entry.setTriggeringPermanentControllerId(creatureControllerId);
                            }
                        });
                if (triggered && read.oneOrMore()) {
                    firedOncePerStepAbilities.add(abilityKey);
                }
            }
        });

        if (gameData.planechase == null || gameData.planechase.controllerId == null
                || gameData.planechase.controllerId.equals(damagedPlayerId)) {
            return;
        }
        UUID planarControllerId = gameData.planechase.controllerId;
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarCombatDamageAbilities(gameData, object, planarControllerId,
                    Triggers.creatureDealsCombatDamageToOpponent(), creature, creatureControllerId);
        }
    }

    /**
     * Hands each ability of the face-up planar card {@code object} that {@code watch} reads to the stack door under
     * {@code controllerId}, its event "a creature deals combat damage" by {@code creature}: the record names "that
     * creature's controller" as the non-targeting target (CR 115.10a), the creature as the triggering permanent, and
     * the planar object the ability is of.
     */
    private void putPlanarCombatDamageAbilities(GameData gameData, PlanarObject object, UUID controllerId,
                                                Trigger watch, Permanent creature, UUID creatureControllerId) {
        Card card = object.getCard();
        ConditionContext context = ConditionContext.forCard(card, controllerId)
                .withTriggeringPermanentId(creature.getId());
        for (TriggeredAbility ability : WatchedAbilities.printed(card, watch, Zone.BATTLEFIELD)) {
            if (Read.effects(List.of(ability), watch).isEmpty()
                    || !conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                continue;
            }
            PlanarObject source = object.copy();
            boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability, card,
                    null, controllerId, card.getName() + "'s triggered ability", context, entry -> {
                        entry.setTargetId(creatureControllerId);
                        entry.setNonTargeting(true);
                        entry.setTriggeringPermanentId(creature.getId());
                        entry.setTriggeringPermanentControllerId(creatureControllerId);
                        entry.setSourcePlanarObject(source);
                    }));
            if (triggered) {
                gameLogService.append(gameData, GameLog.abilityTriggers(card));
            }
        }
    }

    /**
     * Hands the "when you're dealt combat damage" abilities of the damaged player's permanents to the stack door,
     * once for the combat damage dealt to that player in a damage step, the damage as their event value.
     */
    public void checkControllerDealtCombatDamageTriggers(GameData gameData, UUID damagedPlayerId, int damageDealt) {
        if (damagedPlayerId == null || damageDealt <= 0) {
            return;
        }
        Trigger watch = Triggers.youAreDealtCombatDamage();
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt);
        for (Permanent source : new ArrayList<>(gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of()))) {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                continue;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, source, watch)) {
                requirePlainCombatAbility(ability, watch);
                putEventAbility(gameData, ability, source.getCard(), source, damagedPlayerId,
                        ConditionContext.forPermanent(source, damagedPlayerId).withEventValue(damageDealt), event,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(damageDealt);
                        });
            }
        }
    }

    public void checkAnyCreatureCombatDamageToOwnerTriggers(GameData gameData, Permanent creature,
                                                            UUID creatureControllerId,
                                                            UUID damagedPlayerId, int damageDealt) {
        if (creature == null || creatureControllerId == null || damagedPlayerId == null || damageDealt <= 0
                || !gameQueryService.isCreature(gameData, creature)) {
            return;
        }
        UUID creatureOwnerId = gameData.playerCommanders.entrySet().stream()
                .filter(entry -> entry.getValue().stream()
                        .anyMatch(card -> card.getId().equals(creature.getOriginalCard().getId())))
                .map(Map.Entry::getKey).findFirst().orElse(creature.getCard().getOwnerId());
        if (!damagedPlayerId.equals(creatureOwnerId)) return;

        TriggerContext event = new TriggerContext.SourceDealsCombatDamage(creature.getCard(), creatureControllerId,
                creature.getId(), damageDealt, damageDealt);
        gameData.forEachPermanent((controllerId, source) -> {
            if (source.isLosesAllAbilitiesUntilEndOfTurn()) {
                return;
            }

            FilterContext dealerContext = FilterContext.of(gameData).withSourceControllerId(controllerId);
            // Each ability is handed whole to the stack door (CR 603.3), its filter judged once.
            for (List<Read> reads : combatReads(gameData, source,
                    Triggers.creatureDealsCombatDamageToItsOwner())) {
                Read read = reads.getFirst();
                Read.requireNoLimitOrMultiplicity(read,
                        Triggers.creatureDealsCombatDamageToItsOwner());
                // "Whenever a [filter] creature deals combat damage to its owner": the trigger's filter.
                if (read.filter() != null
                        && !predicateEvaluationService.matchesPermanentPredicate(creature, read.filter(), dealerContext)) {
                    continue;
                }
                putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                        ConditionContext.forPermanent(source, controllerId), event, entry -> {
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setNonTargeting(true);
                        });
            }
        });
    }

    /**
     * Fires the batched trigger for one or more goaded creatures dealing combat damage to an
     * opponent. The damage dealers are supplied as one combat-damage batch so multiple goaded
     * creatures cause only one trigger.
     */
    public void checkGoadedCreatureCombatDamageToOpponentTriggers(GameData gameData,
                                                                   UUID damagedPlayerId,
                                                                   List<Permanent> damageDealers) {
        if (damagedPlayerId == null || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }
        Permanent matchingDealer = damageDealers.stream()
                .filter(creature -> gameQueryService.isGoaded(gameData, creature))
                .findFirst()
                .orElse(null);
        if (matchingDealer == null) {
            return;
        }
        UUID dealerControllerId = gameQueryService.findPermanentController(gameData, matchingDealer.getId());
        TriggerContext event = new TriggerContext.SourceDealsCombatDamage(matchingDealer.getCard(), dealerControllerId,
                matchingDealer.getId(), 1, 1);

        gameData.forEachPermanent((controllerId, source) -> {
            if (controllerId.equals(damagedPlayerId)
                    || source.isLosesAllAbilitiesUntilEndOfTurn()
                    || gameQueryService.hasLostPrintedAbilities(gameData, source)) {
                return;
            }

            // "One or more goaded creatures" is the trigger's own: once for the combat damage dealt together.
            for (List<Read> reads : combatReadsWithGrants(gameData, source,
                    Triggers.goadedCreaturesDealCombatDamageToOpponent())) {
                Read read = reads.getFirst();
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The goaded-creatures collector reads no " + read.options() + ": "
                            + read.ability());
                }
                putEventAbility(gameData, read.ability(), source.getCard(), source, controllerId,
                        ConditionContext.forPermanent(source, controllerId), event,
                        entry -> entry.setNonTargeting(true));
            }
        });
    }

    /**
     * Whether {@code subject}, the permanent a damage ability's filter is on (the damage source, the dealer, the
     * damaged permanent), matches {@code filter}, judged with {@code watcher} as the ability's source; {@code false}
     * when there is no such permanent.
     */
    private boolean damageFilterMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                        PermanentPredicate filter, Permanent subject) {
        return subject != null && predicateEvaluationService.matchesPermanentPredicate(subject, filter,
                FilterContext.of(gameData)
                        .withSourceCardId(watcher.getCard().getId())
                        .withSourceControllerId(controllerId)
                        .withSourcePermanentId(watcher.getId())
                        .withSourcePermanentSnapshot(watcher));
    }

    public void checkSourceDealsDamageToPlayerTriggers(GameData gameData, Permanent source,
                                                       UUID controllerId, UUID damagedPlayerId,
                                                       int damageDealt) {
        if (source == null || controllerId == null || damagedPlayerId == null || damageDealt <= 0) {
            return;
        }

        // An effect that removes all abilities removes the source's own triggered abilities, so damage dealt after
        // that loss does not trigger them.
        boolean sourceHasAbilities = !gameQueryService.hasLostAllAbilities(gameData, source);
        Trigger toPlayer = Triggers.dealsDamageToPlayer();
        Trigger toOpponent = Triggers.dealsDamageToOpponent();
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt,
                source.getId(), controllerId);
        // Each ability, printed or granted, of either watch, is one object of all its effects (CR 603.3).
        if (sourceHasAbilities) {
            List<TriggeredAbility> toPlayerAbilities = abilities(gameData, source, toPlayer);
            for (TriggeredAbility ability : toPlayerAbilities) {
                putDealsDamageToPlayerAbility(gameData, ability, toPlayer, source, controllerId, damagedPlayerId,
                        damageDealt, event);
            }
            if (!controllerId.equals(damagedPlayerId)) {
                List<TriggeredAbility> toOpponentAbilities = abilities(gameData, source, toOpponent);
                for (TriggeredAbility ability : toOpponentAbilities) {
                    putDealsDamageToPlayerAbility(gameData, ability, toOpponent, source, controllerId,
                            damagedPlayerId, damageDealt, event);
                }
            }
        }

        // Auras/Equipment attached to the source with ON_DAMAGE_TO_PLAYER (e.g. Curiosity).
        gameData.forEachPermanent((ownerId, perm) -> {
            if (!perm.isAttached() || perm.getAttachedTo() == null
                    || !perm.getAttachedTo().equals(source.getId())) {
                return;
            }
            // Attached triggers are controlled by the Aura/Equipment's controller.
            UUID attachedControllerId = gameData.findControllerOf(perm);
            if (attachedControllerId == null) return;
            for (TriggeredAbility ability : abilities(gameData, perm, toPlayer)) {
                putDealsDamageToPlayerAbility(gameData, ability, toPlayer, perm, attachedControllerId,
                        damagedPlayerId, damageDealt, event);
            }
            if (!ownerId.equals(damagedPlayerId)) {
                for (TriggeredAbility ability : abilities(gameData, perm, toOpponent)) {
                    putDealsDamageToPlayerAbility(gameData, ability, toOpponent, perm, attachedControllerId,
                            damagedPlayerId, damageDealt, event);
                }
            }
        });
    }

    /**
     * Hands a "this deals damage to a player / an opponent" ability of {@code source} to the stack door: its event
     * condition ("deals 6 or more damage", "deals damage to the player who cast Hellish Rebuke", CR 603.2) is judged
     * with the damage as the event's value and the player dealt damage as "that player", and its record names, whatever
     * its effects are, that player as its triggering player and non-targeting "that player", the damage as its event
     * value ("that much"); a target is chosen as it goes on the stack (CR 603.3d), its "that player controls" judged
     * against the player dealt damage.
     */
    private void putDealsDamageToPlayerAbility(GameData gameData, TriggeredAbility ability, Trigger watch,
                                               Permanent source, UUID controllerId, UUID damagedPlayerId, int damage,
                                               TriggerContext event) {
        requireNothingToJudge(ability, watch);
        Permanent snapshot = new Permanent(source);
        putEventAbility(gameData, ability, source.getCard(), source, controllerId,
                ConditionContext.forPermanent(source, controllerId).withTargetId(damagedPlayerId)
                        .withEventValue(damage), event, entry -> {
                    entry.setSourcePermanentSnapshot(snapshot);
                    entry.setNonTargeting(true);
                    entry.setTargetId(damagedPlayerId);
                    entry.setEventValue(damage);
                    entry.setTriggeringPlayerId(damagedPlayerId);
                    entry.setAttackedTargetId(damagedPlayerId);
                });
    }

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, boolean isCombatDamage) {
        checkDamageDealtToControllerTriggers(gameData, damagedPlayerId, sourcePermanentId, isCombatDamage, null);
    }

    public void checkDamageDealtToControllerTriggers(GameData gameData, UUID damagedPlayerId,
                                                     UUID sourcePermanentId, boolean isCombatDamage,
                                                     Set<String> firedBatchedTriggerKeys) {
        if (sourcePermanentId == null) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        boolean hasPlanarCombatDamageTrigger = isCombatDamage
                && hasPlanarCreatureCombatDamageToYouTrigger(gameData, damagedPlayerId);
        boolean hasPlanarDamageTrigger = hasPlanarPermanentDamageToYouTrigger(gameData, damagedPlayerId);
        if (damagedPlayerBattlefield == null && !hasPlanarCombatDamageTrigger && !hasPlanarDamageTrigger) return;

        boolean hasTrigger = false;
        if (damagedPlayerBattlefield != null) {
            for (Permanent perm : gameData.permanentsAndFaceUpSchemes(damagedPlayerId)) {
                if (!abilities(gameData, perm, Triggers.permanentDealsDamageToYou()).isEmpty()
                        || (isCombatDamage
                        && !abilities(gameData, perm, Triggers.creatureDealsCombatDamageToYou()).isEmpty())) {
                    hasTrigger = true;
                    break;
                }
            }
        }
        if (!hasTrigger && !hasPlanarCombatDamageTrigger && !hasPlanarDamageTrigger) return;

        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (sourcePermanent == null) return;

        checkPlanarPermanentDealsDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent, isCombatDamage);

        if (isCombatDamage) {
            if (damagedPlayerBattlefield != null) {
                queueCreatureCombatDamageToYouTriggers(
                        gameData, damagedPlayerId, sourcePermanent, firedBatchedTriggerKeys);
            }
            queuePlanarCreatureCombatDamageToYouTriggers(gameData, damagedPlayerId, sourcePermanent);
        }

        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToController(damagedPlayerId, sourcePermanentId, isCombatDamage);
        Trigger watch = Triggers.permanentDealsDamageToYou();
        Trigger watched = watch;
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);

        for (Permanent perm : new ArrayList<>(gameData.permanentsAndFaceUpSchemes(damagedPlayerId))) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                // If the source was bounced, stop processing all triggers
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (source == null) return;
                requireEachEvent(ability, watch);
                // "Whenever a creature / a Human deals damage to you": the trigger's filter on the dealer.
                PermanentPredicate filter = Read.filter(ability, watched);
                if (filter != null && !damageFilterMatches(gameData, perm, damagedPlayerId, filter, source)) continue;
                putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId)
                                .withTriggeringPermanentId(sourcePermanentId), ctx,
                        damageEventRecord(perm, null, source.getId(), sourceControllerId));
            }
        }
    }

    /**
     * The record of a damage event whose object is the permanent {@code triggeringPermanentId} (the source of the
     * damage, the creature that dealt it, the creature dealt it), whatever the ability's effects are: that permanent as
     * its triggering permanent with its controller as the event happened ({@code triggeringPermanentControllerId}, the
     * fallback once it has left and the controller of a spell source), the damage, when the event has one, as its event
     * value, and {@code watcher} as it was when the ability triggered. The effects read them as they resolve ("destroy
     * it", "that much damage to that creature", "that creature's or spell's controller").
     */
    private static TriggerRecord damageEventRecord(Permanent watcher, Integer damage, UUID triggeringPermanentId,
                                                   UUID triggeringPermanentControllerId) {
        Permanent snapshot = new Permanent(watcher);
        return entry -> {
            entry.setNonTargeting(true);
            if (damage != null) {
                entry.setEventValue(damage);
            }
            entry.setTriggeringPermanentId(triggeringPermanentId);
            entry.setTriggeringPermanentControllerId(triggeringPermanentControllerId);
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Hands every {@code ON_CREATURE_DEALS_COMBAT_DAMAGE_TO_YOU} ability on the damaged
     * player's battlefield and face-up schemes (CR 314.4) to the stack door when a creature dealt them combat damage.
     * The record names the damaging creature as the non-targeting {@code targetId}, so "destroy that creature" is
     * expressed with {@code DestroyTargetPermanentEffect} while the ability itself does not target (CR 115.10a), and
     * as its triggering permanent ("that creature's controller gains control of this land"). An intervening "if" is
     * judged by the door as the ability triggers (CR 603.4; Hixus's "if Hixus entered this turn") and again as it
     * resolves.
     */
    private void queueCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                        Permanent sourceCreature,
                                                        Set<String> firedBatchedTriggerKeys) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)) return;

        Trigger watch = Triggers.creatureDealsCombatDamageToYou();
        TriggerContext event = new TriggerContext.DamageToController(damagedPlayerId, sourceCreature.getId(), true);
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        for (Permanent perm : new ArrayList<>(gameData.permanentsAndFaceUpSchemes(damagedPlayerId))) {
            List<TriggeredAbility> abilities = printedWatchedAbilities(gameData, perm, watch);
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                TriggeredAbility ability = abilities.get(abilityIndex);
                Read read = Read.read(ability, watch).getFirst();
                if (read.limit() != TriggerLimit.NONE || read.eachBlocker()) {
                    throw new IllegalStateException("The \"a creature deals combat damage to you\" collector reads no "
                            + read.options() + ": " + ability);
                }
                // "Whenever a [filter] creature deals combat damage to you": the trigger's filter on the creature.
                if (read.filter() != null && !predicateEvaluationService.matchesPermanentPredicate(sourceCreature,
                        read.filter(), watcherFilterContext(gameData, perm, damagedPlayerId))) {
                    continue;
                }
                // "One or more creatures deal combat damage to you" triggers once for the damage step, each such
                // ability of the permanent on its own (CR 603.3b).
                if (read.oneOrMore() && firedBatchedTriggerKeys != null
                        && firedBatchedTriggerKeys.contains(perm.getId() + ":" + abilityIndex)) {
                    continue;
                }
                Permanent snapshot = new Permanent(perm);
                boolean triggered = putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId), event, entry -> {
                            entry.setTargetId(sourceCreature.getId());
                            entry.setNonTargeting(true);
                            entry.setSourcePermanentSnapshot(snapshot);
                            entry.setTriggeringPermanentId(sourceCreature.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                        });
                if (triggered && read.oneOrMore() && firedBatchedTriggerKeys != null) {
                    firedBatchedTriggerKeys.add(perm.getId() + ":" + abilityIndex);
                }
            }
        }
    }

    /**
     * Handles {@code ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU} — "Whenever enchanted creature
     * deals damage to you, this Aura deals that much damage to that creature's controller" (Backfire).
     *
     * <p>The aura is on its controller's battlefield, so scanning the damaged player's battlefield for auras
     * attached to the damage source naturally restricts the trigger to damage dealt to the aura's controller
     * ("to you"). Called for both combat and non-combat damage dealt to a player. The ability is the Aura's (CR
     * 113.7); its record names the event whatever the effects are: the enchanted creature as the triggering permanent
     * (its id, controller and a snapshot as it dealt the damage) and the damage as the event value, which "that much
     * damage to that creature's controller" reads as the ability resolves.
     */
    public void checkEnchantedCreatureDealtDamageToControllerReflectTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (sourcePermanentId == null || amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        Permanent enchantedCreature = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (enchantedCreature == null) return;

        Trigger watch = Triggers.enchantedCreatureDealsDamageToYou();
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        for (Permanent aura : new ArrayList<>(damagedPlayerBattlefield)) {
            if (!aura.isAttached() || !sourcePermanentId.equals(aura.getAttachedTo())) continue;
            UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (creatureControllerId == null) continue;

            Permanent auraSnapshot = new Permanent(aura);
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, aura, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, aura.getCard(), aura, damagedPlayerId,
                        ConditionContext.forPermanent(aura, damagedPlayerId).withEventValue(amount), event, entry -> {
                            entry.setEventValue(amount);
                            entry.setTriggeringPermanentId(enchantedCreature.getId());
                            entry.setTriggeringCardId(enchantedCreature.getCard().getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setTriggeringPermanent(enchantedCreature);
                            entry.setTriggeringPlayerId(damagedPlayerId);
                            entry.setSourcePermanentSnapshot(auraSnapshot);
                            entry.setNonTargeting(true);
                        });
            }
        }
    }

    /**
     * Handles {@code ON_CONTROLLER_DEALT_DAMAGE} — "Whenever you're dealt damage, ...".
     * Fires once per damage source (per the CR ruling that simultaneous sources trigger separately),
     * carrying the amount so an {@code EventValue} amount ("put that many counters") can read it.
     * Scans the damaged player's own battlefield.
     * <p>
     * Also handles {@code ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT} — "Whenever a source an
     * opponent controls deals damage to you, ..." (Retaliator Griffin) — but only when the damage
     * source is controlled by an opponent of the damaged player. {@code sourceControllerId} is the
     * controller of the damage source (the active player for combat damage to the defender, the
     * spell/ability's controller for non-combat damage); {@code null} disables the opponent-gated slot.
     */
    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkControllerDealtDamageTriggers(gameData, damagedPlayerId, sourceControllerId, amount, true);
    }

    public void checkControllerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount, boolean includeAllSources) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null,
                sourceControllerId);
        boolean fromOpponent = sourceControllerId != null && !sourceControllerId.equals(damagedPlayerId);
        boolean fromAllySource = sourceControllerId != null && sourceControllerId.equals(damagedPlayerId);

        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            if (includeAllSources) {
                putPlayerDamageAbilities(gameData, perm, damagedPlayerId, Triggers.youAreDealtDamage(),
                        ctx, damagedPlayerId, damagedPlayerId, amount, null, sourceControllerId);
            }
            if (fromAllySource) {
                // "Whenever a source you control deals damage to you" (Daredevil, Fearless Fighter's "target
                // opponent"): the damage as its event value; its limit is the door's.
                Trigger allySource = Triggers.youAreDealtDamageBySourceYouControl();
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, allySource)) {
                    requireNothingToJudge(ability, allySource);
                    putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                            ConditionContext.forPermanent(perm, damagedPlayerId).withEventValue(amount), ctx,
                            entry -> entry.setEventValue(amount));
                }
            }
            if (fromOpponent) {
                putPlayerDamageAbilities(gameData, perm, damagedPlayerId,
                        Triggers.youAreDealtDamageBySourceOpponentControls(), ctx, sourceControllerId, damagedPlayerId,
                        amount, null, sourceControllerId);
            }
        }
    }

    /**
     * Hands each printed ability {@code watch} reads of {@code perm} for damage dealt to a player to the stack door
     * (see {@link #playerDamageRecord}); these watches' collectors judge no filter of their own.
     */
    private void putPlayerDamageAbilities(GameData gameData, Permanent perm, UUID controllerId, Trigger watch,
                                          TriggerContext event, UUID thatPlayerId, UUID damagedPlayerId, int amount,
                                          UUID sourcePermanentId, UUID sourceControllerId) {
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
            requireNothingToJudge(ability, watch);
            putEventAbility(gameData, ability, perm.getCard(), perm, controllerId,
                    ConditionContext.forPermanent(perm, controllerId).withTargetId(damagedPlayerId)
                            .withTriggeringPermanentId(sourcePermanentId).withEventValue(amount), event,
                    playerDamageRecord(perm, null, thatPlayerId, damagedPlayerId, amount, sourcePermanentId,
                            sourceControllerId));
        }
    }

    /**
     * The record of a damage-to-a-player ability, whatever its effects are: {@code thatPlayerId} as its non-targeting
     * "that player" (the player dealt damage, or the opponent who controlled the source: Michiko Konda's "that player
     * sacrifices a permanent"), the damage as its event value ("that much", "that many"), the player dealt damage as
     * its triggering player and the player a target "that player controls" is judged against, the source of the
     * damage, when it is a permanent, as its triggering permanent and the source's controller as it dealt the damage as
     * that permanent's controller ("that source's controller", the fallback once it has left and the controller of a
     * spell source), and the ability's source as it was ({@code watcher}, or the face-up plane {@code planarObject}).
     * A target the card declares is chosen as the ability goes on the stack (CR 603.3d); a player the card declares no
     * target for is "that player" (Pain Magnification's "that player discards a card").
     */
    private static TriggerRecord playerDamageRecord(Permanent watcher, PlanarObject planarObject, UUID thatPlayerId,
                                                    UUID damagedPlayerId, int amount, UUID sourcePermanentId,
                                                    UUID sourceControllerId) {
        Permanent snapshot = watcher == null ? null : new Permanent(watcher);
        PlanarObject plane = planarObject == null ? null : planarObject.copy();
        return entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(thatPlayerId);
            entry.setEventValue(amount);
            entry.setTriggeringPlayerId(damagedPlayerId);
            entry.setAttackedTargetId(damagedPlayerId);
            entry.setTriggeringPermanentId(sourcePermanentId);
            entry.setTriggeringPermanentControllerId(sourceControllerId);
            if (snapshot != null) {
                entry.setSourcePermanentSnapshot(snapshot);
            }
            if (plane != null) {
                entry.setSourcePlanarObject(plane);
            }
        };
    }

    /**
     * Handles {@code ON_ALLY_SOURCE_DEALS_DAMAGE_TO_OPPONENT} — "Whenever a source you
     * control deals damage to another player, ..." (Night Dealings). The outbound mirror of
     * {@link #checkControllerDealtDamageTriggers}: it scans the damage source's controller's own
     * battlefield, and only fires when the damaged player is someone else.
     * The source permanent ID is carried for effects that are self-scoped within this slot.
     */
    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        checkAllySourceDealtDamageToOpponentTriggers(gameData, damagedPlayerId, sourceControllerId, null, amount);
    }

    public void checkAllySourceDealtNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int amount) {
        if (amount <= 0 || sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        var ctx = new TriggerContext.NoncombatDamageToOpponent(damagedPlayerId, sourceControllerId, amount);
        for (Permanent permanent : List.copyOf(battlefield)) {
            putPlayerDamageAbilities(gameData, permanent, sourceControllerId,
                    Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), ctx, damagedPlayerId,
                    damagedPlayerId, amount, null, sourceControllerId);
        }
    }

    public void checkAllySourceDealtDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, UUID sourcePermanentId, int amount) {
        if (amount <= 0 || sourceControllerId == null) return;

        if (!sourceControllerId.equals(damagedPlayerId)) {
            checkPlanarAllySourceDealtDamageToOpponentTriggers(
                    gameData, damagedPlayerId, sourceControllerId, sourcePermanentId, amount);
        } else {
            checkPlanarSourceDealtDamageToItsControllerTriggers(
                    gameData, damagedPlayerId, sourcePermanentId, amount);
        }

        List<Permanent> sourceControllerBattlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (sourceControllerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId,
                sourceControllerId);
        for (Permanent perm : List.copyOf(sourceControllerBattlefield)) {
            for (Trigger watch : List.of(Triggers.sourceYouControlDealsDamageToOpponent(),
                    Triggers.sourceYouControlDealsDamageToPlayer())) {
                if (watch.equals(Triggers.sourceYouControlDealsDamageToOpponent())
                        && sourceControllerId.equals(damagedPlayerId)) continue;
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                    requireEachEvent(ability, watch);
                    // "Whenever a [filtered] source you control deals damage": the filter is on the source.
                    PermanentPredicate filter = Read.filter(ability, watch);
                    if (filter != null && !damageFilterMatches(gameData, perm, sourceControllerId, filter,
                            sourcePermanentId == null ? null
                                    : gameQueryService.findPermanentById(gameData, sourcePermanentId))) {
                        continue;
                    }
                    // The ability's event condition and intervening "if", with the damage and its source (Dragonborn
                    // Champion's "5 or more damage", Talon of Pain's "other than this artifact", CR 603.4).
                    putEventAbility(gameData, ability, perm.getCard(), perm, sourceControllerId,
                            ConditionContext.forPermanent(perm, sourceControllerId)
                                    .withTriggeringPermanentId(sourcePermanentId).withEventValue(amount), ctx,
                            playerDamageRecord(perm, null, damagedPlayerId, damagedPlayerId, amount,
                                    sourcePermanentId, sourceControllerId));
                }
            }
        }
    }

    /**
     * Handles effects that trigger whenever an opponent is dealt damage, regardless of the damage
     * source's controller.
     */
    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        checkOpponentDealtDamageTriggers(gameData, damagedPlayerId, null, amount);
    }

    public void checkOpponentDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourcePermanentId, int amount) {
        if (amount <= 0 || damagedPlayerId == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, sourcePermanentId);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            if (watcherPlayerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                putPlayerDamageAbilities(gameData, perm, watcherPlayerId, Triggers.opponentDealtDamage(), ctx,
                        damagedPlayerId, damagedPlayerId, amount, sourcePermanentId, null);
            }
        });
    }

    /**
     * Handles {@code ON_CREATURE_DEALS_DAMAGE_TO_YOU_OR_YOUR_PERMANENT} — "Whenever a
     * creature of the chosen color deals damage to you or a white creature you control, ...".
     * Scans the damaged player's battlefield for watchers and judges each ability's filter on the
     * creature and its damaged filter on the damaged permanent (damage to the player always counts).
     *
     * @param damagedPlayerId  the damaged player, or the damaged permanent's controller
     * @param damagedPermanent the damaged permanent, or {@code null} when the player was damaged
     */
    public void checkCreatureDamageToYouOrYourPermanentTriggers(GameData gameData, UUID damagedPlayerId,
            Permanent damagedPermanent, Permanent damageSource, int damage) {
        if (damage <= 0 || damagedPlayerId == null || damageSource == null) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (battlefield == null) return;

        if (!gameQueryService.isCreature(gameData, damageSource)) return;

        var ctx = new TriggerContext.CreatureDamageToYouOrYourPermanent(
                damageSource, damagedPlayerId, damagedPermanent, damage);
        Trigger watch = Triggers.creatureDealsDamageToYouOrYourPermanent();
        Trigger watched = watch;
        UUID damageSourceControllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
        for (Permanent perm : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireEachEvent(ability, watch);
                DamageTrigger trigger = Read.trigger(ability, watched);
                // "A creature of the chosen color": the trigger's filter on the creature; "to you or a white creature
                // you control": its damaged filter on the permanent, damage to you always counting.
                if (trigger.filter() != null && !damageFilterMatches(gameData, perm, damagedPlayerId,
                        trigger.filter(), damageSource)) {
                    continue;
                }
                if (damagedPermanent != null && trigger.damagedFilter() != null && !damageFilterMatches(gameData,
                        perm, damagedPlayerId, trigger.damagedFilter(), damagedPermanent)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId).withEventValue(damage), ctx,
                        damageEventRecord(perm, damage, damageSource.getId(), damageSourceControllerId));
            }
        }
    }

    /**
     * Fires triggers for each player or permanent recipient of damage during one damage event.
     * A permanent recipient is retained separately from its controller because cards may trigger
     * once for each permanent damaged, in addition to a trigger for damage dealt to the player.
     */
    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Set<UUID> damagedPlayerIds) {
        if (sourceCard == null || sourceControllerId == null || damagedPlayerIds == null
                || damagedPlayerIds.isEmpty()) return;

        List<SourceDamageRecipient> recipients = damagedPlayerIds.stream()
                .filter(Objects::nonNull)
                .map(playerId -> new SourceDamageRecipient(playerId, null))
                .toList();
        checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, recipients);
    }

    public void checkOpponentSourceDamageToYouOrYourPermanentTriggers(GameData gameData, Card sourceCard,
            UUID sourceControllerId, UUID sourcePermanentId, Collection<SourceDamageRecipient> recipients) {
        if (sourceCard == null || sourceControllerId == null || recipients == null || recipients.isEmpty()) return;

        for (SourceDamageRecipient recipient : recipients) {
            // "A source an opponent controls": the damage dealt to its controller or their permanents doesn't count.
            if (recipient == null || recipient.damagedPlayerId() == null
                    || sourceControllerId.equals(recipient.damagedPlayerId())) continue;
            UUID damagedPlayerId = recipient.damagedPlayerId();
            List<Permanent> battlefield = gameData.playerBattlefields.get(damagedPlayerId);
            if (battlefield == null) continue;

            TriggerContext.SourceDamageToYouOrYourPermanent ctx =
                    new TriggerContext.SourceDamageToYouOrYourPermanent(
                            sourceCard, sourceControllerId, sourcePermanentId, damagedPlayerId,
                            recipient.damagedPermanentId());
            for (Permanent perm : List.copyOf(battlefield)) {
                // "That player" is the opponent who controlled the source as it dealt the damage (Elesh Norn's "that
                // source's controller loses 2 life unless they pay {1}").
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm,
                        Triggers.sourceOpponentControlsDealsDamageToYouOrYourPermanent())) {
                    requireNothingToJudge(ability, Triggers.sourceOpponentControlsDealsDamageToYouOrYourPermanent());
                    putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                            ConditionContext.forPermanent(perm, damagedPlayerId).withTargetId(damagedPlayerId)
                                    .withTriggeringPermanentId(sourcePermanentId), ctx,
                            playerDamageRecord(perm, null, sourceControllerId, damagedPlayerId, 0,
                                    sourcePermanentId, sourceControllerId));
                }
            }
        }
    }

    public record SourceDamageRecipient(UUID damagedPlayerId, UUID damagedPermanentId) {
    }

    /**
     * Collects {@code watcher}'s printed "a source [you control] deals damage" abilities of {@code watches} for one
     * source's damage in one event. Each ability triggers when the trigger's filters match the source
     * ({@link #damageSourceMatches}) and its event condition holds, and goes to the stack door whole (CR 603.3): the
     * source permanent its triggering permanent, the source's controller as it dealt the damage the fallback for "its
     * controller" (a spell's), the summed damage its event value ({@code eventValue}: the damage the source dealt in
     * all, or to you for "deals damage to you"). An ability with a target has it chosen as the ability goes on the
     * stack (CR 603.3d; Circle of Affliction's "target player").
     */
    private void collectSourceDealsDamageAbilities(GameData gameData, Permanent watcher, UUID controllerId,
            List<Trigger> watches, TriggerContext.SourceDealsDamage event, int eventValue) {
        for (Trigger watch : watches) {
            Trigger watched = watch;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                requireEachEvent(ability, watch);
                if (!damageSourceMatches(gameData, watcher, controllerId,
                        Read.trigger(ability, watched), event)) {
                    continue;
                }
                putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                        ConditionContext.forPermanent(watcher, controllerId).withEventValue(eventValue), event,
                        damageEventRecord(watcher, eventValue, event.sourcePermanentId(), event.sourceControllerId()));
            }
        }
    }

    /**
     * Collects {@code watcher}'s printed "a source deals damage to a permanent or player" abilities for one source's
     * damage in one event, once for each permanent and player it dealt damage to (CR 603.2c; Ghyrson Starn's ruling):
     * {@code ON_ANY_SOURCE_DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} always, and
     * {@code ON_ANOTHER_ALLY_SOURCE_DEALS_DAMAGE_TO_PERMANENT_OR_PLAYER} when {@code controllerId} controls
     * the source and the watcher isn't it.
     */
    private void collectSourceDealsDamageToPermanentOrPlayerAbilities(GameData gameData, Permanent watcher,
            UUID controllerId, TriggerContext.SourceDealsDamage event) {
        List<Trigger> watches = new ArrayList<>(2);
        watches.add(Triggers.sourceDealsDamageToPermanentOrPlayer());
        if (controllerId.equals(event.sourceControllerId()) && !watcher.getId().equals(event.sourcePermanentId())) {
            watches.add(Triggers.anotherSourceYouControlDealsDamageToPermanentOrPlayer());
        }
        if (watches.stream().allMatch(watch -> abilities(gameData, watcher, watch).isEmpty())) {
            return;
        }
        event.damageToPlayers().forEach((playerId, damage) -> {
            if (damage > 0) {
                collectDamageRecipientAbilities(gameData, watcher, controllerId, watches, event, playerId, true,
                        damage);
            }
        });
        event.damageToPermanents().forEach((permanentId, damage) -> {
            if (damage > 0) {
                collectDamageRecipientAbilities(gameData, watcher, controllerId, watches, event, permanentId, false,
                        damage);
            }
        });
    }

    /**
     * Collects {@code watcher}'s abilities of {@code watches} for the damage {@code event}'s source dealt to one
     * recipient ({@code recipientId}, a player when {@code playerRecipient}). Each ability triggers when the trigger's
     * filters match the source ({@link #damageSourceMatches}) and the recipient ({@link #damageRecipientMatches}) and
     * its event condition holds, judged with the recipient as the context's target (and triggering permanent) and the
     * damage dealt to it as the event value (CR 603.2: Ghyrson Starn's "exactly 1 damage"); the door judges its
     * intervening "if" in the same context (CR 603.4). Its record is {@link #damageRecipientRecord}'s.
     */
    private void collectDamageRecipientAbilities(GameData gameData, Permanent watcher, UUID controllerId,
            List<Trigger> watches, TriggerContext.SourceDealsDamage event, UUID recipientId,
            boolean playerRecipient, int damage) {
        ConditionContext context = ConditionContext.forPermanent(watcher, controllerId).withTargetId(recipientId)
                .withEventValue(damage);
        if (!playerRecipient) {
            context = context.withTriggeringPermanentId(recipientId);
        }
        for (Trigger watch : watches) {
            Trigger watched = watch;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                requireEachEvent(ability, watch);
                DamageTrigger trigger = Read.trigger(ability, watched);
                if (!damageSourceMatches(gameData, watcher, controllerId, trigger, event)
                        || !damageRecipientMatches(gameData, watcher, controllerId, trigger, event, recipientId,
                        playerRecipient)) {
                    continue;
                }
                putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId, context, event,
                        damageRecipientRecord(gameData, watcher, recipientId, playerRecipient, damage));
            }
        }
    }

    /**
     * Collects every permanent's printed "whenever damage is dealt to a player" abilities
     * ({@code ON_PLAYER_DEALT_DAMAGE}) for the damage {@code damagedPlayerId} was dealt in one damage event,
     * whatever sources dealt it: the ability triggers once for the event, not once for each source (CR 603.2c; Boros
     * Reckoner's ruling). The callers sum the event's damage to the player: {@code CombatDamageService} the combat
     * damage of one combat damage step (CR 510.2), {@code DamageSupport.flushSourceDamageReflections} the noncombat
     * damage of one resolution damage event. Each ability triggers when its damaged-player filter matches the player
     * (judged with the watcher as the predicate's source) and its event condition holds, judged with the player as the
     * context's target and the damage as the event value. Its record is {@link #damageRecipientRecord}'s.
     */
    public void checkPlayerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId, int damage) {
        if (damage <= 0 || damagedPlayerId == null) return;
        Trigger watch = Triggers.playerDealtDamage();
        Trigger watched = watch;
        var event = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damage);
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent watcher : List.copyOf(battlefield)) {
                for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                    requireEachEvent(ability, watch);
                    DamageTrigger trigger = Read.trigger(ability, watched);
                    if (trigger.damagedPlayerFilter() != null && !targetLegalityService.matchesPlayerPredicate(
                            gameData, controllerId, damagedPlayerId, trigger.damagedPlayerFilter(), watcher.getId())) {
                        continue;
                    }
                    putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                            ConditionContext.forPermanent(watcher, controllerId).withTargetId(damagedPlayerId)
                                    .withEventValue(damage), event,
                            damageRecipientRecord(gameData, watcher, damagedPlayerId, true, damage));
                }
            }
        });
    }

    /**
     * Whether the recipient of a source's damage matches the recipient filters of {@code trigger}: a player by its
     * damaged-player filter, a permanent by its damaged filter (on its snapshot as it was dealt the damage, or as it is
     * now). A trigger with neither filter matches every recipient; one with a filter only for one kind of recipient
     * matches no recipient of the other kind.
     */
    private boolean damageRecipientMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                           DamageTrigger trigger, TriggerContext.SourceDealsDamage event,
                                           UUID recipientId, boolean playerRecipient) {
        if (trigger.damagedFilter() == null && trigger.damagedPlayerFilter() == null) return true;
        if (playerRecipient) {
            return trigger.damagedPlayerFilter() != null && targetLegalityService.matchesPlayerPredicate(
                    gameData, controllerId, recipientId, trigger.damagedPlayerFilter(), watcher.getId());
        }
        if (trigger.damagedFilter() == null) return false;
        Permanent damaged = event.damagedPermanentSnapshots().get(recipientId);
        if (damaged == null) {
            damaged = gameQueryService.findPermanentById(gameData, recipientId);
        }
        return damaged != null && damageFilterMatches(gameData, watcher, controllerId, trigger.damagedFilter(), damaged);
    }

    /**
     * The record of a "deals damage to a permanent or player" or "damage is dealt to a player" ability of
     * {@code watcher}, whatever its effects are: the recipient as its non-targeting {@code targetId} ("that permanent
     * or player"), a player also its triggering player and a permanent its triggering permanent with that permanent's
     * controller, the damage dealt to it as its event value, and the watcher as it was when the ability triggered.
     */
    private TriggerRecord damageRecipientRecord(GameData gameData, Permanent watcher, UUID recipientId,
                                                boolean playerRecipient, int damage) {
        UUID recipientControllerId = playerRecipient ? null
                : gameQueryService.findPermanentController(gameData, recipientId);
        Permanent snapshot = new Permanent(watcher);
        return entry -> {
            entry.setNonTargeting(true);
            entry.setTargetId(recipientId);
            entry.setEventValue(damage);
            if (playerRecipient) {
                entry.setTriggeringPlayerId(recipientId);
            } else {
                entry.setTriggeringPermanentId(recipientId);
                entry.setTriggeringPermanentControllerId(recipientControllerId);
            }
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Whether the source of {@code event} matches the filters of {@code trigger}, judged with {@code watcher} as the
     * ability's source as the damage is dealt: a permanent source by the trigger's filter (on its card's
     * characteristics once it has left the battlefield), any other source (a spell, a card dealing damage from another
     * zone) by its spell filter. A trigger with neither filter matches every source; one with a filter only for one
     * kind of source matches no source of the other kind.
     */
    private boolean damageSourceMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                        DamageTrigger trigger, TriggerContext.SourceDealsDamage event) {
        if (trigger.filter() == null && trigger.spellFilter() == null) return true;
        if (event.sourcePermanentId() != null) {
            if (trigger.filter() == null) return false;
            Permanent source = gameQueryService.findPermanentById(gameData, event.sourcePermanentId());
            return damageFilterMatches(gameData, watcher, controllerId, trigger.filter(),
                    source != null ? source : new Permanent(event.sourceCard()));
        }
        return trigger.spellFilter() != null && predicateEvaluationService.matchesCardPredicate(event.sourceCard(),
                trigger.spellFilter(), watcher.getCard().getId(), gameData, event.sourceControllerId());
    }

    /**
     * Collects the abilities of the instant or sorcery spell's controller's permanents that watch it deal damage, for
     * the batched damage event (already summed across simultaneous targets): "whenever an instant or sorcery spell you
     * control deals damage" ({@code ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE}), "... that targets only a
     * single creature deals damage to that creature" and "... deals damage to an opponent", once for each opponent.
     */
    public void checkAllyInstantOrSorcerySpellDealsDamageTriggers(GameData gameData,
                                                                   PendingSourceDamage batch) {
        Card sourceCard = batch.getSourceCard();
        UUID sourceControllerId = batch.getControllerId();
        if (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY)) {
            return;
        }
        var context = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId,
                batch.getSourcePermanentId(), batch.getAmount(), batch.getDamageToPlayers(),
                batch.getSingleCreatureSpellTargetId(), batch.getDamageToPermanents(), false);
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        UUID onlyTargetCreatureId = batch.getSingleCreatureSpellTargetId();
        int damageToOnlyTargetCreature = onlyTargetCreatureId == null ? 0
                : batch.getDamageToPermanents().getOrDefault(onlyTargetCreatureId, 0);
        for (Permanent permanent : List.copyOf(battlefield)) {
            // Blaze Commando: "Whenever an instant or sorcery spell you control deals damage, ...", once for the
            // batched damage event: the damage dealt as its event value and the watcher as it was, which a transform
            // instruction reads to tell whether it has already transformed since (CR 701.27f).
            Trigger dealsDamage = Triggers.instantOrSorceryYouControlDealsDamage();
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, permanent, dealsDamage)) {
                requireNothingToJudge(ability, dealsDamage);
                Permanent snapshot = new Permanent(permanent);
                putEventAbility(gameData, ability, permanent.getCard(), permanent, sourceControllerId,
                        ConditionContext.forPermanent(permanent, sourceControllerId)
                                .withEventValue(batch.getAmount()), context, entry -> {
                            entry.setEventValue(batch.getAmount());
                            entry.setSourcePermanentSnapshot(snapshot);
                        });
            }
            if (damageToOnlyTargetCreature > 0) {
                collectInstantOrSorceryDealsDamageToItsOnlyTargetCreatureAbilities(gameData, permanent,
                        sourceControllerId, onlyTargetCreatureId, damageToOnlyTargetCreature, context);
            }
            batch.getDamageToPlayers().forEach((damagedPlayerId, damage) -> {
                if (damage <= 0) {
                    return;
                }
                queueInstantOrSorceryDealsDamageToPlayerTriggers(gameData, permanent, sourceControllerId,
                        damagedPlayerId, damage, Triggers.instantOrSorceryYouControlDealsDamageToPlayer(),
                        context);
                if (!damagedPlayerId.equals(sourceControllerId)) {
                    queueInstantOrSorceryDealsDamageToPlayerTriggers(gameData, permanent, sourceControllerId,
                            damagedPlayerId, damage, Triggers.instantOrSorceryYouControlDealsDamageToOpponent(),
                            context);
                }
            });
        }
    }

    /**
     * "Whenever an instant or sorcery spell you control that targets only a single creature deals damage to that
     * creature" ({@code ON_ALLY_INSTANT_OR_SORCERY_DEALS_DAMAGE_TO_ITS_ONLY_TARGET_CREATURE}, Imodane): each
     * ability goes to the stack door whole (CR 603.3), that creature its triggering permanent and the damage dealt to
     * it ("that much") its event value.
     */
    private void collectInstantOrSorceryDealsDamageToItsOnlyTargetCreatureAbilities(GameData gameData,
            Permanent watcher, UUID controllerId, UUID creatureId, int damage, TriggerContext.SourceDealsDamage event) {
        Trigger watch = Triggers.instantOrSorceryYouControlDealsDamageToItsOnlyTargetCreature();
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
            requireNothingToJudge(ability, watch);
            putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                    ConditionContext.forPermanent(watcher, controllerId).withEventValue(damage), event,
                    damageEventRecord(watcher, damage, creatureId,
                            gameQueryService.findPermanentController(gameData, creatureId)));
        }
    }

    /**
     * "Whenever an instant or sorcery spell you control deals damage to an opponent"
     * ({@code Triggers.instantOrSorceryYouControlDealsDamageToOpponent()}) or "... to a player"
     * ({@code Triggers.instantOrSorceryYouControlDealsDamageToPlayer()}, Syr Carah, the Bold) for one player dealt damage:
     * each player is an occurrence of its own (CR 603.2c). "That player" is the ability's triggering player and "that
     * much" its event value; a target is chosen as the ability goes on the stack (CR 603.3d), its "that player
     * controls" judged against the player dealt damage.
     */
    private void queueInstantOrSorceryDealsDamageToPlayerTriggers(GameData gameData, Permanent permanent,
                                                                  UUID controllerId, UUID damagedPlayerId,
                                                                  int damage, Trigger watch,
                                                                  TriggerContext event) {
        for (TriggeredAbility ability : printedWatchedAbilities(gameData, permanent, watch)) {
            requireNothingToJudge(ability, watch);
            Permanent snapshot = new Permanent(permanent);
            putEventAbility(gameData, ability, permanent.getCard(), permanent, controllerId,
                    ConditionContext.forPermanent(permanent, controllerId).withTargetId(damagedPlayerId)
                            .withEventValue(damage), event, entry -> {
                        entry.setEventValue(damage);
                        entry.setTriggeringPlayerId(damagedPlayerId);
                        entry.setSourcePermanentSnapshot(snapshot);
                        entry.setAttackedTargetId(damagedPlayerId);
                    });
        }
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, null);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers, snapshottedSelfAbilities, null, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers, snapshottedSelfAbilities, singleCreatureSpellTargetId, damageToPermanents, false);
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents,
                                                   boolean combatDamage) {
        queueSourceDealsDamageReflections(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedSelfAbilities, singleCreatureSpellTargetId,
                damageToPermanents, combatDamage, Map.of());
    }

    public void queueSourceDealsDamageReflections(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                   UUID sourcePermanentId, int totalDamage,
                                                   Map<UUID, Integer> damageToPlayers,
                                                   List<TriggeredAbility> snapshottedSelfAbilities,
                                                   UUID singleCreatureSpellTargetId,
                                                   Map<UUID, Integer> damageToPermanents,
                                                   boolean combatDamage,
                                                   Map<UUID, Permanent> damagedPermanentSnapshots) {
        if (sourceCard == null || sourceControllerId == null || totalDamage <= 0) return;

        var ctx = new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, sourcePermanentId, totalDamage,
                damageToPlayers == null ? Map.of() : Map.copyOf(damageToPlayers), singleCreatureSpellTargetId,
                damageToPermanents == null ? Map.of() : Map.copyOf(damageToPermanents), combatDamage,
                damagedPermanentSnapshots);
        gameData.forEachBattlefield((watcherPlayerId, battlefield) -> {
            List<Trigger> watches = watcherPlayerId.equals(sourceControllerId)
                    ? List.of(Triggers.sourceDealsDamage(), Triggers.sourceYouControlDealsDamage())
                    : List.of(Triggers.sourceDealsDamage());
            int damageToWatcherController = ctx.damageToPlayers().getOrDefault(watcherPlayerId, 0);
            for (Permanent perm : List.copyOf(battlefield)) {
                collectSourceDealsDamageAbilities(gameData, perm, watcherPlayerId, watches, ctx, totalDamage);
                if (damageToWatcherController > 0) {
                    collectSourceDealsDamageAbilities(gameData, perm, watcherPlayerId,
                            List.of(Triggers.sourceDealsDamageToYou()), ctx, damageToWatcherController);
                }
                collectSourceDealsDamageToPermanentOrPlayerAbilities(gameData, perm, watcherPlayerId, ctx);
            }
        });

        // Self triggers (El-Hajjâj): only the damage source's own "whenever this creature deals
        // damage" abilities fire. Keyed off the source card (not a battlefield scan) so it still
        // triggers when the source died dealing that damage.
        Permanent sourcePermanent = findPermanentByCardId(gameData, sourceCard.getId());
        List<TriggeredAbility> selfAbilities = snapshottedSelfAbilities != null
                ? new ArrayList<>(snapshottedSelfAbilities)
                : sourcePermanent != null ? abilities(gameData, sourcePermanent, Triggers.dealsDamage())
                : new ArrayList<>(Read.printed(sourceCard, Triggers.dealsDamage()));
        // The source may have died dealing the damage: its ability keeps its last-known permanent as its source.
        Permanent abilitySource = sourcePermanent != null ? sourcePermanent
                : lastKnownSource(gameData, sourceCard, sourcePermanentId);
        for (TriggeredAbility ability : selfAbilities) {
            requireEachEvent(ability, Triggers.dealsDamage());
            // "Whenever this deals damage to one or more creatures": the filter is on the damaged permanents.
            PermanentPredicate filter = Read.filter(ability, Triggers.dealsDamage());
            if (filter != null && !damagedPermanentMatches(gameData, ctx, filter, sourcePermanent)) {
                continue;
            }
            // The ability's intervening "if" and event condition, with the damage dealt (CR 603.4).
            ConditionContext selfContext = sourcePermanent != null
                    ? ConditionContext.forPermanent(sourcePermanent, sourceControllerId)
                    : ConditionContext.forCard(sourceCard, sourceControllerId);
            putEventAbility(gameData, ability, sourceCard, abilitySource, sourceControllerId,
                    selfContext.withEventValue(ctx.totalDamage()), ctx,
                    entry -> entry.setEventValue(ctx.totalDamage()));
        }

        for (DelayedWatchedCreatureDealsDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealsDamage.class)) {
            if (!watch.watchedPermanentId().equals(sourcePermanentId)) continue;

            StackEntry trigger = TriggeredAbilityStackService.madeAbility(watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(totalDamage);
            triggeredAbilityStackService.putDelayed(gameData, trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    /**
     * Whether a permanent {@code ctx}'s source dealt damage to in this event, as it was before state-based actions,
     * matches {@code filter}: the filter of "whenever this deals damage to one or more [permanents]", judged with the
     * damage source as the ability's source.
     */
    private boolean damagedPermanentMatches(GameData gameData, TriggerContext.SourceDealsDamage ctx,
                                            PermanentPredicate filter, Permanent sourcePermanent) {
        FilterContext recipientContext = FilterContext.of(gameData)
                .withSourceControllerId(ctx.sourceControllerId())
                .withSourceCardId(ctx.sourceCard().getId())
                .withSourcePermanentId(ctx.sourcePermanentId())
                .withSourcePermanentSnapshot(sourcePermanent);
        return ctx.damageToPermanents().entrySet().stream()
                .filter(damage -> damage.getValue() > 0)
                .map(damage -> ctx.damagedPermanentSnapshots().get(damage.getKey()))
                .filter(Objects::nonNull)
                .anyMatch(recipient -> predicateEvaluationService.matchesPermanentPredicate(
                        recipient, filter, recipientContext));
    }

    /**
     * "Whenever enchanted creature deals damage, …": each ability of an Aura attached to {@code sourceCreature} goes to
     * the stack door whole (CR 603.3), the ability the Aura's (CR 113.7); its record names, whatever its effects are,
     * the Aura's controller as the non-targeting "you", the damage as its event value and the enchanted creature, with
     * its controller as it dealt the damage, as its triggering permanent ("its controller").
     */
    public void queueEnchantedCreatureDealsDamageTriggers(GameData gameData, Permanent sourceCreature,
                                                           int damageDealt) {
        if (sourceCreature == null || damageDealt <= 0) return;

        Trigger watch = Triggers.enchantedCreatureDealsDamage();
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            Permanent auraSnapshot = new Permanent(aura);
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, aura, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, aura.getCard(), aura, auraControllerId,
                        ConditionContext.forPermanent(aura, auraControllerId)
                                .withTriggeringPermanentId(sourceCreature.getId()).withEventValue(damageDealt), null,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setTargetId(auraControllerId);
                            entry.setEventValue(damageDealt);
                            entry.setTriggeringPermanentId(sourceCreature.getId());
                            entry.setTriggeringPermanentControllerId(sourceControllerId);
                            entry.setSourcePermanentSnapshot(auraSnapshot);
                        });
            }
        });
    }

    /**
     * "Whenever enchanted creature deals damage to a creature, …" for {@code sourceCreature} dealing damage to the
     * creature {@code damagedCreatureId}, judged as the damage is dealt, before state-based actions, so the ability
     * still triggers when either combat creature dies from the same damage event: the damaged creature is its
     * non-targeting "that creature".
     */
    public void queueEnchantedCreatureDealsDamageToCreatureTriggers(GameData gameData,
                                                                      Permanent sourceCreature,
                                                                      UUID damagedCreatureId,
                                                                      int damageDealt) {
        if (sourceCreature == null || damagedCreatureId == null || damageDealt <= 0
                || sourceCreature.getId().equals(damagedCreatureId)
                || !gameQueryService.isCreature(gameData, sourceCreature)) {
            return;
        }

        Trigger watch = Triggers.enchantedCreatureDealsDamageToCreature();
        gameData.forEachPermanent((auraControllerId, aura) -> {
            if (!aura.isAttached() || !sourceCreature.getId().equals(aura.getAttachedTo())) return;

            for (TriggeredAbility ability : printedWatchedAbilities(gameData, aura, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, aura.getCard(), aura, auraControllerId,
                        ConditionContext.forPermanent(aura, auraControllerId)
                                .withTriggeringPermanentId(damagedCreatureId).withEventValue(damageDealt), null,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setTargetId(damagedCreatureId);
                        });
            }
        });
    }

    /**
     * The permanent {@code sourceCard} was as it last existed on the battlefield, when it was the permanent
     * {@code sourcePermanentId} that left it (CR 608.2h), or {@code null}.
     */
    private static Permanent lastKnownSource(GameData gameData, Card sourceCard, UUID sourcePermanentId) {
        if (sourcePermanentId == null) {
            return null;
        }
        Permanent departed = gameData.departedPermanentSnapshots.get(sourceCard.getId());
        return departed != null && sourcePermanentId.equals(departed.getId()) ? departed : null;
    }

    /** The battlefield permanent whose card has this id, or null once it has left the battlefield. */
    private Permanent findPermanentByCardId(GameData gameData, UUID cardId) {
        List<Permanent> found = new ArrayList<>(1);
        gameData.forEachPermanent((playerId, perm) -> {
            if (perm.getCard().getId().equals(cardId)) {
                found.add(perm);
            }
        });
        return found.isEmpty() ? null : found.getFirst();
    }

    /**
     * Dispatches combat-damage-only self triggers from the source card. The source permanent ID is
     * retained even when state-based actions removed the source before triggered abilities were put
     * on the stack, so effects that require the source to have survived can check it at resolution.
     */
    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      UUID damagedPlayerId,
                                                      List<TriggeredAbility> snapshottedSelfAbilities) {
        queueSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, damagedPlayerId, snapshottedSelfAbilities, null);
    }

    /**
     * Captures, per controller, the permanents with "whenever a creature you control deals combat
     * damage" abilities before state-based actions remove them: the event happens while they are
     * still on the battlefield (CR 603.2), so one destroyed by that same damage still triggers. A
     * permanent that has lost its printed abilities then (Song of the Dryads) has none to trigger.
     */
    public Map<UUID, List<Permanent>> snapshotAllyCreatureDealsCombatDamageWatchers(GameData gameData) {
        Map<UUID, List<Permanent>> watchers = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            if (!abilities(gameData, permanent, Triggers.creatureYouControlDealsCombatDamage()).isEmpty()) {
                watchers.computeIfAbsent(controllerId, ignored -> new ArrayList<>()).add(permanent);
            }
        });
        return watchers;
    }

    public void queueSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                      UUID sourceControllerId, UUID sourcePermanentId,
                                                      int totalDamage, int damageToPlayers,
                                                      UUID damagedPlayerId,
                                                      List<TriggeredAbility> snapshottedSelfAbilities,
                                                      List<Permanent> snapshottedAllyWatchers) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) {
            return;
        }
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers, damagedPlayerId);
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedSelfAbilities, Triggers.dealsCombatDamage());

        // "Whenever a creature you control deals combat damage" watchers (Five-Alarm Fire). Scanned on
        // the damage source's controller's battlefield only; the watcher itself needn't be a creature.
        // The damage source counts as a creature as it currently is, so an animated noncreature
        // permanent (Gruul Keyrune) qualifies, while a printed creature that stopped being one does not.
        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        boolean damageSourceIsCreature = damageSource != null
                ? gameQueryService.isCreature(gameData, damageSource)
                : sourceCard.hasType(CardType.CREATURE);
        if (!damageSourceIsCreature) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;
        List<Permanent> allyWatchers = snapshottedAllyWatchers != null
                ? snapshottedAllyWatchers : List.copyOf(battlefield);
        Trigger allyWatch = Triggers.creatureYouControlDealsCombatDamage();
        for (Permanent watcher : allyWatchers) {
            if (snapshottedAllyWatchers == null && gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
                continue;
            }
            // The watcher, not the damage dealer, is the ability's source, so a self-referencing effect (Five-Alarm
            // Fire's blaze counter) lands on the watcher; the damage dealer is the triggering permanent, so a
            // TRIGGERING-referenced effect (Quilled Greatwurm's counters) lands on it.
            for (TriggeredAbility ability : WatchedAbilities.printed(watcher.getCard(), allyWatch, Zone.BATTLEFIELD)) {
                requirePlainCombatAbility(ability, allyWatch);
                putEventAbility(gameData, ability, watcher.getCard(), watcher, sourceControllerId,
                        ConditionContext.forPermanent(watcher, sourceControllerId), ctx, entry -> {
                            entry.setTriggeringPermanentId(sourcePermanentId);
                            entry.setNonTargeting(true);
                            entry.setEventValue(totalDamage);
                        });
            }
        }

        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            // Each ability's event condition ("during your turn", The Aetherspark; CR 603.2) and intervening "if"
            // (CR 603.4), read from the Equipment and its controller as the ability triggers.
            if (damageToPlayers > 0) {
                putEquipmentCombatDamageAbilities(gameData, watcher, watcherControllerId,
                        Triggers.equippedCreatureDealsCombatDamageToPlayer(), ctx, damageToPlayers);
            }
            putEquipmentCombatDamageAbilities(gameData, watcher, watcherControllerId,
                    Triggers.equippedCreatureDealsCombatDamage(), ctx, totalDamage);
        });
    }

    /**
     * Hands each "whenever equipped creature deals combat damage [to a player]" ability {@code watch} reads of the
     * Equipment {@code equipment} to the stack door, the ability the Equipment's (CR 113.7): "that player" is the
     * player dealt damage, if any (the non-targeting target and triggering player), the equipped creature its
     * triggering permanent and {@code damage} its event value ("that much" counts only the damage dealt to players for
     * "deals combat damage to a player").
     */
    private void putEquipmentCombatDamageAbilities(GameData gameData, Permanent equipment, UUID controllerId,
                                                   Trigger watch, TriggerContext.SourceDealsCombatDamage ctx,
                                                   int damage) {
        Permanent snapshot = new Permanent(equipment);
        for (TriggeredAbility ability : WatchedAbilities.printed(equipment.getCard(), watch, Zone.BATTLEFIELD)) {
            requirePlainCombatAbility(ability, watch);
            putEventAbility(gameData, ability, equipment.getCard(), equipment, controllerId,
                    ConditionContext.forPermanent(equipment, controllerId)
                            .withTriggeringPermanentId(ctx.sourcePermanentId()), ctx, entry -> {
                        if (ctx.damagedPlayerId() != null) {
                            entry.setTargetId(ctx.damagedPlayerId());
                        }
                        entry.setTriggeringPlayerId(ctx.damagedPlayerId());
                        entry.setTriggeringPermanentId(ctx.sourcePermanentId());
                        entry.setNonTargeting(true);
                        entry.setEventValue(damage);
                        entry.setSourcePermanentSnapshot(snapshot);
                    });
        }
    }

    /** Throws when {@code ability} has a filter, limit or multiplicity, none of which {@code watch}'s collector reads. */
    private static void requirePlainCombatAbility(TriggeredAbility ability, Trigger watch) {
        Read read = Read.read(ability, watch).getFirst();
        Read.requireNoLimitOrMultiplicity(read, watch);
        if (read.filter() != null) {
            throw new IllegalStateException("The collector of " + watch + " judges no filter: " + ability);
        }
    }

    /** Hands the player-or-battle combat-damage abilities of Equipment attached to the source to the stack door. */
    private void queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData,
                                                                                  Card sourceCard,
                                                                                  UUID sourceControllerId,
                                                                                  UUID sourcePermanentId,
                                                                                  int totalDamage,
                                                                                  int damageToPlayers) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcher.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                    || !isEquippedBy(gameData, watcher, sourcePermanentId)) {
                return;
            }
            putEquipmentCombatDamageAbilities(gameData, watcher, watcherControllerId,
                    Triggers.equippedCreatureDealsCombatDamageToPlayerOrBattle(), ctx, totalDamage);
        });
    }

    /**
     * Handles effects that trigger whenever one or more creatures controlled by a player deal
     * damage to a player. The dealers are supplied as one batch so the trigger fires once for the
     * event even when multiple creatures dealt damage simultaneously; the player dealt damage is the ability's
     * non-targeting "that player" (Francisco, Fowl Marauder; The Thing, Ben Grimm).
     */
    public void checkAllyCreaturesDealDamageToPlayerTriggers(GameData gameData, UUID sourceControllerId,
                                                              UUID damagedPlayerId,
                                                              List<Permanent> damageDealers) {
        if (sourceControllerId == null || damagedPlayerId == null
                || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) {
            return;
        }

        var context = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                damagedPlayerId, sourceControllerId, damageDealers);
        Trigger toPlayer = Triggers.creaturesYouControlDealDamageToPlayer();
        for (Permanent permanent : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : allyCreaturesDealDamageAbilities(gameData, permanent, toPlayer)) {
                requireEachEvent(ability, toPlayer);
                // "One or more [filtered] creatures you control": the filter is on each creature that dealt damage.
                if (!anyDealerMatches(gameData, permanent, sourceControllerId,
                        Read.filter(ability, toPlayer), damageDealers, true)) {
                    continue;
                }
                putAllyCreaturesDealDamageAbility(gameData, ability, permanent, sourceControllerId, damagedPlayerId,
                        context);
            }
        }
    }

    /** Hands a "whenever one or more creatures you control deal damage to a player [an opponent]" ability to the door. */
    private void putAllyCreaturesDealDamageAbility(GameData gameData, TriggeredAbility ability, Permanent watcher,
                                                   UUID controllerId, UUID damagedPlayerId, TriggerContext event) {
        putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId,
                ConditionContext.forPermanent(watcher, controllerId).withTargetId(damagedPlayerId), event, entry -> {
                    entry.setTargetId(damagedPlayerId);
                    entry.setNonTargeting(true);
                });
    }

    /**
     * Handles effects that trigger whenever one or more creatures controlled by a player deal
     * damage to an opponent. The dealers are supplied as one batch so the trigger fires once for
     * the event even when multiple creatures dealt damage simultaneously.
     */
    public void checkAllyCreaturesDealDamageToOpponentTriggers(GameData gameData, UUID sourceControllerId,
                                                                UUID damagedPlayerId,
                                                                List<Permanent> damageDealers) {
        checkAllyCreaturesDealDamageToOpponentTriggers(gameData, sourceControllerId, damagedPlayerId,
                damageDealers, snapshotAllyCreaturesDealDamageToOpponentTriggers(gameData)
                        .getOrDefault(sourceControllerId, List.of()));
    }

    /** Watcher and its abilities at the time the damage event occurred. */
    public record AllyDamageTriggerSnapshot(Permanent permanent, List<TriggeredAbility> abilities) {
        public AllyDamageTriggerSnapshot {
            permanent = new Permanent(permanent);
            abilities = List.copyOf(abilities);
        }
    }

    /**
     * The "whenever one or more creatures you control deal damage to a player [opponent]" abilities {@code watch}
     * reads of {@code permanent}: its printed ones unless it lost them, then those granted to it for a duration and by
     * other permanents' static abilities.
     */
    private List<TriggeredAbility> allyCreaturesDealDamageAbilities(GameData gameData, Permanent permanent,
                                                                    Trigger watch) {
        return abilities(gameData, permanent, watch);
    }

    /**
     * Whether one of {@code dealers} matches the filter of "whenever one or more [filtered] creatures you control deal
     * damage" ({@code null}: any), judged with {@code watcher} as the ability's source; with {@code creaturesOnly},
     * only creatures count.
     */
    private boolean anyDealerMatches(GameData gameData, Permanent watcher, UUID controllerId,
                                     PermanentPredicate filter, List<Permanent> dealers, boolean creaturesOnly) {
        return dealers.stream()
                .filter(dealer -> !creaturesOnly || gameQueryService.isCreature(gameData, dealer))
                .anyMatch(dealer -> filter == null
                        || damageFilterMatches(gameData, watcher, controllerId, filter, dealer));
    }

    /** Captures watchers before damage casualties remove them or the effects granting their abilities. */
    public Map<UUID, List<AllyDamageTriggerSnapshot>> snapshotAllyCreaturesDealDamageToOpponentTriggers(
            GameData gameData) {
        Map<UUID, List<AllyDamageTriggerSnapshot>> snapshots = new LinkedHashMap<>();
        gameData.forEachPermanent((controllerId, permanent) -> {
            List<TriggeredAbility> abilities = allyCreaturesDealDamageAbilities(gameData, permanent,
                    Triggers.creaturesYouControlDealDamageToOpponent());
            if (!abilities.isEmpty()) {
                snapshots.computeIfAbsent(controllerId, ignored -> new ArrayList<>())
                        .add(new AllyDamageTriggerSnapshot(permanent, abilities));
            }
        });
        return snapshots;
    }

    public void checkAllyCreaturesDealDamageToOpponentTriggers(GameData gameData, UUID sourceControllerId,
                                                               UUID damagedPlayerId, List<Permanent> damageDealers,
                                                               List<AllyDamageTriggerSnapshot> watchers) {
        if (sourceControllerId == null || damagedPlayerId == null
                || sourceControllerId.equals(damagedPlayerId)
                || damageDealers == null || damageDealers.isEmpty()) {
            return;
        }

        var context = new TriggerContext.AllyCreaturesDealDamageToPlayer(
                damagedPlayerId, sourceControllerId, damageDealers);
        Trigger toOpponent = Triggers.creaturesYouControlDealDamageToOpponent();
        for (AllyDamageTriggerSnapshot watcher : watchers) {
            for (TriggeredAbility ability : watcher.abilities()) {
                requireEachEvent(ability, toOpponent);
                PermanentPredicate filter = Read.filter(ability, toOpponent);
                // "One or more Pirates you control" counts any object with the type, so a Kindred Pirate spell is a
                // dealer too; only an unfiltered trigger is limited to creatures.
                if (!anyDealerMatches(gameData, watcher.permanent(), sourceControllerId, filter, damageDealers,
                        filter == null)) {
                    continue;
                }
                putAllyCreaturesDealDamageAbility(gameData, ability, watcher.permanent(), sourceControllerId,
                        damagedPlayerId, context);
            }
        }
    }

    private boolean hasPlanarCreatureCombatDamageToYouTrigger(GameData gameData, UUID damagedPlayerId) {
        return gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                && gameData.planechase.faceUp.stream()
                .anyMatch(object -> !WatchedAbilities.printed(object.getCard(), Triggers.creatureDealsCombatDamageToYou(), Zone.BATTLEFIELD).isEmpty());
    }

    private void queuePlanarCreatureCombatDamageToYouTriggers(GameData gameData, UUID damagedPlayerId,
                                                               Permanent sourceCreature) {
        if (!gameQueryService.isCreature(gameData, sourceCreature)
                || gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, damagedPlayerId)) {
            return;
        }

        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, sourceCreature.getId());
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarCombatDamageAbilities(gameData, object, damagedPlayerId,
                    Triggers.creatureDealsCombatDamageToYou(), sourceCreature, creatureControllerId);
        }
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                                   UUID damageSourceControllerId) {
        checkDealtDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                null, null);
    }

    public void checkDealtDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                                   UUID damageSourceControllerId, Card sourceCard,
                                                   UUID sourcePermanentId) {
        if (damageDealt > 0) {
            checkEnchantedCreatureDealtDamageTriggers(gameData, damagedCreature, damageDealt);
        }

        checkDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId, Triggers.dealtDamage());
    }

    /** Fires triggers whose event specifically requires noncombat damage dealt to this permanent. */
    public void checkNoncombatDamageToSelfTriggers(GameData gameData, Permanent damagedCreature,
                                                   int damageDealt, UUID damageSourceControllerId,
                                                   Card sourceCard, UUID sourcePermanentId) {
        checkDamageToCreatureTriggers(gameData, damagedCreature, damageDealt, damageSourceControllerId,
                sourceCard, sourcePermanentId, Triggers.dealtNoncombatDamage());
    }

    private void checkDamageToCreatureTriggers(GameData gameData, Permanent damagedCreature, int damageDealt,
                                               UUID damageSourceControllerId, Card sourceCard,
                                               UUID sourcePermanentId, Trigger watch) {
        Trigger watched = watch;
        List<TriggeredAbility> abilities = abilities(gameData, damagedCreature, watch);
        if (abilities.isEmpty()) return;

        UUID controllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        if (controllerId == null) return;

        collectDealtDamageAbilities(gameData, Read.readByAbility(abilities, watched), watch,
                damagedCreature, controllerId, new TriggerContext.DamageToCreature(damagedCreature, damageDealt,
                        damageSourceControllerId, sourceCard, sourcePermanentId));
    }

    /**
     * Hands the "whenever this is dealt damage" abilities {@code abilities} (one list of reads for each ability, each
     * one object on the stack, CR 603.3) of {@code damaged} (controlled by {@code controllerId}) to the stack door for
     * the damage {@code ctx} names: for noncombat damage as it is dealt, and for the combat damage step's (which reads
     * the abilities as the damage is dealt and hands them on once its creatures have died) with the permanent's last
     * known information. Each ability's event condition is judged as it triggers, and its intervening "if" and limit
     * by the door (CR 603.4), with the damage as the event's value, the source as the triggering permanent and its
     * controller as "that player" (Archfiend of Spite's "a source an opponent controls"). The ability is the damaged
     * permanent's (CR 113.7); its record names the damage as its event value ("that much", CR 120.8: no damage, no
     * trigger), the source of the damage as its triggering permanent (if it was one) with that source's controller as it
     * dealt the damage ("that source's controller"), and the permanent as it was dealt the damage, with the damage
     * keywords it had, as its source snapshot ("it deals that much damage" once it has died, CR 608.2h).
     */
    public void collectDealtDamageAbilities(GameData gameData, List<List<Read>> abilities,
                                            Trigger watch, Permanent damaged, UUID controllerId,
                                            TriggerContext.DamageToCreature ctx) {
        if (ctx.damageDealt() <= 0 || controllerId == null) return;
        ConditionContext conditionContext = ConditionContext.forPermanent(damaged, controllerId)
                .withTargetId(ctx.damageSourceControllerId())
                .withTriggeringPermanentId(ctx.sourcePermanentId())
                .withEventValue(ctx.damageDealt());
        Permanent lastKnown = ctx.damagedLastKnown() != null
                ? ctx.damagedLastKnown() : lastKnownDamageSource(gameData, damaged);
        // "Once for each source" (CR 603.2c): each damage event the collector is given has one source, or is the
        // combat damage step's to this permanent.
        for (List<Read> ability : abilities) {
            Read read = ability.getFirst();
            if (read.filter() != null) {
                throw new IllegalStateException("The collector of " + watch + " judges no filter: " + read.ability());
            }
            putEventAbility(gameData, read.ability(), damaged.getCard(), damaged, controllerId, conditionContext, ctx,
                    entry -> {
                        entry.setEventValue(ctx.damageDealt());
                        entry.setTriggeringPermanentId(ctx.sourcePermanentId());
                        entry.setTriggeringPermanentControllerId(ctx.damageSourceControllerId());
                        entry.setSourcePermanentSnapshot(lastKnown);
                    });
        }
    }

    /**
     * "Whenever enchanted (equipped) creature is dealt damage, …": the ability is the Aura's or Equipment's (CR 113.7),
     * whatever its effects are. Its record names the creature as the triggering permanent (its id, controller, power
     * and a snapshot with the damage keywords it had, its last known information once it has left, CR 608.2h) and the
     * damage dealt as the event value; "it" / "its controller" / "that much" read them as the ability resolves. A
     * targeted ability's target is chosen as it goes on the stack (CR 603.3d).
     */
    public void checkEnchantedCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature, int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToCreature(damagedCreature, damageDealt, null);
        Trigger watch = Triggers.enchantedCreatureDealtDamage();
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !perm.getAttachedTo().equals(damagedCreature.getId())) {
                return;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, auraOwnerId,
                        ConditionContext.forPermanent(perm, auraOwnerId)
                                .withTriggeringPermanentId(damagedCreature.getId()).withEventValue(damageDealt), ctx,
                        damagedPermanentRecord(gameData, perm, damagedCreature,
                                gameQueryService.findPermanentController(gameData, damagedCreature.getId()),
                                damageDealt, true, false));
            }
        });
    }

    /**
     * The record of a "[a permanent] is dealt damage" ability of {@code watcher}, whatever its effects are: the damaged
     * permanent as its triggering permanent with its card ("that card"), its controller as it was dealt the damage
     * ("that creature's controller"), its last known information with the damage keywords it had ("it deals that much
     * damage", CR 608.2h) and, with {@code recordPower}, its power, the damage as its event value ("that much"), the
     * watcher as it was when the ability triggered, and, with {@code namedObject}, the damaged permanent as the
     * non-targeting "that creature" (CR 115.10a).
     */
    private TriggerRecord damagedPermanentRecord(GameData gameData, Permanent watcher, Permanent damaged,
                                                 UUID damagedControllerId, int damage, boolean recordPower,
                                                 boolean namedObject) {
        Permanent lastKnown = lastKnownDamageSource(gameData, damaged);
        Integer power = recordPower ? gameQueryService.getEffectivePower(gameData, damaged) : null;
        Permanent snapshot = new Permanent(watcher);
        return entry -> {
            entry.setNonTargeting(true);
            if (namedObject) {
                entry.setTargetId(damaged.getId());
            }
            entry.setEventValue(damage);
            entry.setTriggeringPermanentId(damaged.getId());
            entry.setTriggeringCardId(damaged.getCard().getId());
            entry.setTriggeringPermanentControllerId(damagedControllerId);
            entry.setTriggeringPermanent(lastKnown);
            if (power != null) {
                entry.setTriggeringPermanentPowerAtTrigger(power);
            }
            entry.setSourcePermanentSnapshot(snapshot);
        };
    }

    /**
     * Last known information for a creature whose own ability deals damage after it may have died:
     * the damage-relevant keywords it had from other permanents' static abilities are baked in.
     */
    private Permanent lastKnownDamageSource(GameData gameData, Permanent creature) {
        Permanent snapshot = new Permanent(creature);
        for (Keyword keyword : List.of(Keyword.LIFELINK, Keyword.DEATHTOUCH, Keyword.INFECT, Keyword.WITHER)) {
            if (gameQueryService.hasKeyword(gameData, creature, keyword)) {
                snapshot.getGrantedKeywords().add(keyword);
            }
        }
        return snapshot;
    }

    /**
     * "Whenever enchanted player is dealt damage" (Grievous Wound): the ability is the Curse's (CR 113.7), the player
     * its non-targeting "that player" and the damage its event value.
     */
    public void checkEnchantedPlayerDealtDamageTriggers(GameData gameData, UUID damagedPlayerId,
            int damageDealt) {
        if (damageDealt <= 0) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, damageDealt);
        Trigger watch = Triggers.enchantedPlayerDealtDamage();
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !damagedPlayerId.equals(perm.getAttachedTo())) {
                return;
            }
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, auraOwnerId,
                        ConditionContext.forPermanent(perm, auraOwnerId).withTargetId(damagedPlayerId)
                                .withEventValue(damageDealt), ctx, entry -> {
                            entry.setTargetId(damagedPlayerId);
                            entry.setNonTargeting(true);
                            entry.setEventValue(damageDealt);
                        });
            }
        });
    }

    /**
     * Fires ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers on permanents whose controller
     * is different from the damaged creature's controller (i.e. the damaged creature is
     * an opponent's creature from the perspective of the permanent's controller).
     * Called once per damaged creature — each call produces one trigger per listening permanent.
     */
    public void checkOpponentCreatureDealtDamageTriggers(GameData gameData, UUID damagedCreatureControllerId) {
        Trigger watch = Triggers.creatureOpponentControlsDealtDamage();
        TriggerContext event = new TriggerContext.DamageToControllerAmount(damagedCreatureControllerId, 0);
        gameData.forEachPermanent((playerId, perm) -> {
            // Only fire when the damaged creature was controlled by an opponent of this permanent's controller
            if (playerId.equals(damagedCreatureControllerId)) return;
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId), event, entry -> {
                        });
            }
        });
    }

    /** Fires turn-scoped global triggers for damage dealt to an opponent's creature. */
    public void checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
            GameData gameData, Permanent damagedCreature, UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreature == null || damagedCreatureControllerId == null || damageDealt <= 0) return;

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(Triggers.creatureOpponentControlsDealtDamage())
                    || watcher.controllerId().equals(damagedCreatureControllerId)) {
                continue;
            }
            enqueueTemporaryGlobalTrigger(gameData, watcher, damagedCreature.getId(), damageDealt);
        }
    }

    /**
     * Fires excess-damage triggers on permanents whose controller is different from the damaged
     * permanent's controller. Triggers from the {@code ON_OPPONENT_CREATURE_OR_PLANESWALKER_DEALT_EXCESS_NONCOMBAT_DAMAGE}
     * slot fire only when {@code noncombatDamage} is true; combat excess damage still records the excess. The record
     * names the permanent dealt the excess damage as the non-targeting "that permanent" and triggering permanent, and
     * the excess as the event value; a target the card declares ("any target other than that permanent", Toralf) is
     * chosen as the ability goes on the stack (CR 603.3d).
     */
    public void checkOpponentPermanentDealtExcessDamageTriggers(GameData gameData,
                                                                Permanent damagedPermanent,
                                                                UUID damagedPermanentControllerId,
                                                                int excessDamage,
                                                                boolean noncombatDamage) {
        if (damagedPermanent == null || damagedPermanentControllerId == null || excessDamage <= 0
                || (!gameQueryService.isCreature(gameData, damagedPermanent)
                && !damagedPermanent.getCard().hasType(CardType.PLANESWALKER))) return;

        gameData.recordControllerOfPermanentDealtExcessDamageThisTurn(damagedPermanentControllerId);

        if (gameQueryService.isCreature(gameData, damagedPermanent)) {
            gameData.recordPermanentDealtExcessDamageThisTurn(damagedPermanent.getId());
        }

        List<Trigger> watches = noncombatDamage
                ? List.of(Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessDamage(),
                        Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessNoncombatDamage())
                : List.of(Triggers.creatureOrPlaneswalkerOpponentControlsDealtExcessDamage());
        TriggerContext context = new TriggerContext.OpponentPermanentDealtExcessDamage(
                damagedPermanent, damagedPermanentControllerId, excessDamage);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedPermanentControllerId)) return;

            for (Trigger watch : watches) {
                List<TriggeredAbility> abilities = abilities(gameData, perm, watch);
                for (TriggeredAbility ability : abilities) {
                    requireNothingToJudge(ability, watch);
                    putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                            ConditionContext.forPermanent(perm, playerId).withTargetId(damagedPermanent.getId())
                                    .withTriggeringPermanentId(damagedPermanent.getId())
                                    .withEventValue(excessDamage), context, entry -> {
                                entry.setTargetId(damagedPermanent.getId());
                                entry.setNonTargeting(true);
                                entry.setEventValue(excessDamage);
                                entry.setTriggeringPermanentId(damagedPermanent.getId());
                                entry.setTriggeringPermanentControllerId(damagedPermanentControllerId);
                            });
                }
            }
        });
    }

    public void checkOpponentCreatureDealtExcessNoncombatDamageTriggers(GameData gameData,
                                                                          StackEntry damageEntry,
                                                                          Permanent damagedCreature,
                                                                          UUID damagedCreatureControllerId,
                                                                          int excessDamage) {
        if (damageEntry == null || damagedCreature == null || damagedCreatureControllerId == null
                || excessDamage <= 0 || !gameQueryService.isCreature(gameData, damagedCreature)) return;

        Trigger watch = Triggers.creaturesOpponentsControlDealtExcessNoncombatDamage();
        TriggerContext context = new TriggerContext.OpponentPermanentDealtExcessDamage(
                damagedCreature, damagedCreatureControllerId, excessDamage);
        gameData.forEachPermanent((playerId, perm) -> {
            if (playerId.equals(damagedCreatureControllerId)) return;

            List<TriggeredAbility> abilities = abilities(gameData, perm, watch);
            // Each ability, once for the damage event ("one or more creatures"), marked by its place among the
            // permanent's abilities of the watch.
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                TriggeredAbility ability = abilities.get(abilityIndex);
                requireNothingToJudge(ability, watch);
                if (!damageEntry.markNoncombatExcessDamageTriggerFired(perm.getId(), abilityIndex)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId).withTargetId(damagedCreature.getId()),
                        context, entry -> {
                            entry.setTargetId(damagedCreature.getId());
                            entry.setNonTargeting(true);
                        });
            }
        });
    }

    /**
     * Collects the "deals damage to a creature" abilities (the four "deals damage to a creature"
     * triggers) for one creature dealing damage to one creature: the abilities other permanents watch, then the damage
     * source's own. Called once per source/target/damage event; {@code combatDamage} says whether it was combat damage
     * for the combat-only triggers ({@link DamageTrigger#combatOnly()}).
     */
    public void checkAllyDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        checkWatchersDealtDamageToCreatureTriggers(gameData, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
        checkSelfDealtDamageToCreatureTriggers(gameData, damageSource, damageSourceControllerId,
                damagedCreatureControllerId, damagedCreatureId, damagedCreature, damage, combatDamage);
    }

    /**
     * Collects the "deals damage to a creature" abilities of the permanents other than the damage source: "a creature
     * you control" on its controller's permanents, "equipped creature" on the Equipment attached to it (whoever
     * controls that Equipment) and "a creature" on every permanent. The source's own abilities are collected by
     * {@link #checkSelfDealtDamageToCreatureTriggers}.
     */
    public void checkWatchersDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null
                || damagedCreatureId == null || damage <= 0) {
            return;
        }
        DealsDamageToCreatureEvent event = new DealsDamageToCreatureEvent(damageSource, damageSourceControllerId,
                damagedCreatureId, damagedCreatureControllerId, damagedCreature, damage, combatDamage);
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            // The damage source watches itself below, whether or not it survived the damage.
            if (watcher.getId().equals(damageSource.getId())) return;
            List<Trigger> watches = new ArrayList<>(3);
            if (watcherControllerId.equals(damageSourceControllerId)) {
                watches.add(Triggers.creatureYouControlDealsDamageToCreature());
            }
            if (isEquippedBy(gameData, watcher, damageSource)) {
                watches.add(Triggers.equippedCreatureDealsDamageToCreature());
            }
            watches.add(Triggers.creatureDealsDamageToCreature());
            collectDealsDamageToCreatureAbilities(gameData, watcher, watcherControllerId, watches, false, event);
        });
    }

    /**
     * Collects the damage source's own "deals damage to a creature" abilities ("this creature", and "a creature you
     * control" and "a creature", which include it), the ones it has been granted among them. Combat damage calls this
     * before state-based actions so the grant is still present when lethal damage back from the blocker or a granting
     * permanent dying in the same step has already moved the source off the battlefield.
     */
    public void checkSelfDealtDamageToCreatureTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedCreatureControllerId, UUID damagedCreatureId,
            Permanent damagedCreature, int damage, boolean combatDamage) {
        if (damageSource == null || damageSourceControllerId == null || damagedCreatureControllerId == null
                || damagedCreatureId == null || damage <= 0) {
            return;
        }
        collectDealsDamageToCreatureAbilities(gameData, damageSource, damageSourceControllerId,
                List.of(Triggers.dealsDamageToCreature(),
                        Triggers.creatureYouControlDealsDamageToCreature(),
                        Triggers.creatureDealsDamageToCreature()), true,
                new DealsDamageToCreatureEvent(damageSource, damageSourceControllerId, damagedCreatureId,
                        damagedCreatureControllerId, damagedCreature, damage, combatDamage));
    }

    public void checkAllyDealtDamageToPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damageSourceControllerId, UUID damagedPlaneswalkerId, int damage, boolean combatDamage) {
        checkCreatureDealsDamageToEnchantedPlaneswalkerTriggers(
                gameData, damageSource, damagedPlaneswalkerId, damage, combatDamage);
        if (damageSource == null || damageSourceControllerId == null || damagedPlaneswalkerId == null || damage <= 0) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)
                || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage);
        // "Deals combat damage to a planeswalker" (Zagras, Thief of Heartbeats) only for combat damage.
        List<Trigger> watches = combatDamage
                ? List.of(Triggers.creatureYouControlDealsDamageToPlaneswalker(),
                        Triggers.creatureYouControlDealsCombatDamageToPlaneswalker())
                : List.of(Triggers.creatureYouControlDealsDamageToPlaneswalker());
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!watcherControllerId.equals(damageSourceControllerId)) return;
            for (Trigger toPlaneswalker : watches) {
                List<TriggeredAbility> abilities = abilities(gameData, watcher, toPlaneswalker);
                for (TriggeredAbility ability : abilities) {
                    requireEachEvent(ability, toPlaneswalker);
                    // "Whenever a [filtered] creature you control deals damage to a planeswalker": on the creature.
                    PermanentPredicate filter = Read.filter(ability, toPlaneswalker);
                    if (filter != null && !damageFilterMatches(gameData, watcher, watcherControllerId, filter,
                            damageSource)) {
                        continue;
                    }
                    // "Destroy that planeswalker" (Zagras): the planeswalker is the non-targeting "that
                    // planeswalker", the creature the triggering permanent.
                    putEventAbility(gameData, ability, watcher.getCard(), watcher, watcherControllerId,
                            ConditionContext.forPermanent(watcher, watcherControllerId)
                                    .withTriggeringPermanentId(damageSource.getId()).withEventValue(damage), context,
                            entry -> {
                                entry.setTargetId(damagedPlaneswalkerId);
                                entry.setTriggeringPermanentId(damageSource.getId());
                                entry.setNonTargeting(true);
                            });
                }
            }
        });
    }

    /**
     * "Whenever a creature deals damage to enchanted planeswalker, …": the creature is the non-targeting "that
     * creature" and the triggering permanent of each such ability of an Aura attached to the planeswalker.
     */
    public void checkCreatureDealsDamageToEnchantedPlaneswalkerTriggers(GameData gameData, Permanent damageSource,
            UUID damagedPlaneswalkerId, int damage, boolean combatDamage) {
        if (damageSource == null || damagedPlaneswalkerId == null || damage <= 0
                || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }
        Permanent planeswalker = gameQueryService.findPermanentById(gameData, damagedPlaneswalkerId);
        if (planeswalker == null || !planeswalker.getCard().hasType(CardType.PLANESWALKER)) {
            return;
        }

        TriggerContext context = new TriggerContext.CreatureDealsDamageToPlaneswalker(
                damageSource, damagedPlaneswalkerId, damage, combatDamage);
        Trigger watch = Triggers.creatureDealsDamageToEnchantedPlaneswalker();
        gameData.forEachPermanent((watcherControllerId, watcher) -> {
            if (!damagedPlaneswalkerId.equals(watcher.getAttachedTo())) return;
            List<TriggeredAbility> abilities = abilities(gameData, watcher, watch);
            for (TriggeredAbility ability : abilities) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, watcher.getCard(), watcher, watcherControllerId,
                        ConditionContext.forPermanent(watcher, watcherControllerId)
                                .withTriggeringPermanentId(damageSource.getId()).withEventValue(damage), context,
                        entry -> {
                            entry.setTargetId(damageSource.getId());
                            entry.setTriggeringPermanentId(damageSource.getId());
                            entry.setNonTargeting(true);
                        });
            }
        });
    }

    public void queueSourceDealsCombatDamageToPlayerOrPlaneswalkerTriggers(GameData gameData, Card sourceCard,
                                                                            UUID sourceControllerId,
                                                                            UUID sourcePermanentId,
                                                                            int totalDamage,
                                                                            int damageToPlayers,
                                                                            List<TriggeredAbility> snapshottedAbilities) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedAbilities,
                Triggers.dealsCombatDamageToPlayerOrPlaneswalker());
    }

    public void queueSourceDealsCombatDamageToPlayerOrBattleTriggers(GameData gameData, Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       UUID sourcePermanentId,
                                                                       int totalDamage,
                                                                       int damageToPlayers,
                                                                       List<TriggeredAbility> snapshottedAbilities) {
        if (sourceCard == null || sourceControllerId == null || sourcePermanentId == null || totalDamage <= 0) return;
        dispatchSourceDealsCombatDamageTriggers(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                totalDamage, damageToPlayers, snapshottedAbilities,
                Triggers.dealsCombatDamageToPlayerOrBattle());
        queueEquippedCreatureDealsCombatDamageToPlayerOrBattleTriggers(
                gameData, sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
    }

    /**
     * Hands the damage source's own "whenever this deals combat damage [to a player or planeswalker / battle]"
     * abilities to the stack door: its printed ones and those granted to it ({@code snapshottedAbilities}, as the
     * combat damage step read them as the damage was dealt, else as it has them now). The source may have died dealing
     * the damage: the ability keeps its last-known permanent as its source (CR 608.2h), and the damage dealt is its
     * event value. A "choose one" announces its mode (CR 603.3c) and a target is chosen (CR 603.3d) as it goes on the
     * stack.
     */
    private void dispatchSourceDealsCombatDamageTriggers(GameData gameData, Card sourceCard,
                                                         UUID sourceControllerId, UUID sourcePermanentId,
                                                         int totalDamage, int damageToPlayers,
                                                         List<TriggeredAbility> snapshottedAbilities,
                                                         Trigger watch) {
        var ctx = new TriggerContext.SourceDealsCombatDamage(
                sourceCard, sourceControllerId, sourcePermanentId, totalDamage, damageToPlayers);
        List<TriggeredAbility> abilities = new ArrayList<>(WatchedAbilities.printed(sourceCard, watch, Zone.BATTLEFIELD));
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (snapshottedAbilities != null) {
            // The granted abilities as the combat damage step snapshotted them.
            abilities.addAll(snapshottedAbilities);
        } else if (sourcePermanent != null) {
            abilities.addAll(WatchedAbilities.temporary(sourcePermanent, watch));
            abilities.addAll(WatchedAbilities.persistent(sourcePermanent, watch));
        }
        Permanent abilitySource = sourcePermanent != null ? sourcePermanent
                : lastKnownSource(gameData, sourceCard, sourcePermanentId);
        ConditionContext context = sourcePermanent != null
                ? ConditionContext.forPermanent(sourcePermanent, sourceControllerId)
                : ConditionContext.forCard(sourceCard, sourceControllerId);
        for (TriggeredAbility ability : abilities) {
            requirePlainCombatAbility(ability, watch);
            putEventAbility(gameData, ability, sourceCard, abilitySource, sourceControllerId,
                    context.withEventValue(totalDamage), ctx, entry -> entry.setEventValue(totalDamage));
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(watch)
                    || !sourcePermanentId.equals(watcher.watchedPermanentId())) continue;
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(Read.effects(List.of(watcher.ability()), watch)),
                    null,
                    sourcePermanentId);
            entry.setEventValue(totalDamage);
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * One creature dealing damage to one creature, as the "deals damage to a creature" collector reads it.
     *
     * @param damageSource                the creature that dealt the damage
     * @param damageSourceControllerId    its controller as it dealt the damage
     * @param damagedCreatureId           the creature dealt the damage
     * @param damagedCreatureControllerId that creature's controller as it was dealt the damage
     * @param damagedCreature             that creature, or its snapshot before state-based actions; may be {@code null}
     * @param damage                      the damage dealt
     * @param combatDamage                whether it was combat damage
     */
    private record DealsDamageToCreatureEvent(Permanent damageSource, UUID damageSourceControllerId,
                                              UUID damagedCreatureId, UUID damagedCreatureControllerId,
                                              Permanent damagedCreature, int damage, boolean combatDamage) {
    }

    /**
     * Collects {@code watcher}'s abilities of {@code watches} for {@code event}: the printed ones (none while face down,
     * CR 708.2), the ones granted until a time or until it leaves, and, when {@code readGrants} (the damage source's
     * own), the ones a static ability grants it, unless it has lost its abilities. Each ability triggers when its
     * trigger's filters match (the creature that dealt the damage, the creature dealt it, combat only) and its event
     * condition holds, judged with the creature dealt the damage as the context's triggering permanent (Maarika's "if
     * that creature was dealt excess damage this turn"), and goes to the stack door whole (CR 603.3): the creature dealt
     * the damage is its triggering permanent (with that creature's controller as it was dealt the damage, for "that
     * creature's controller" once it has left), the creature that dealt it its event damage source (for "that Archer
     * deals …"), the damage its event value; the effects read them as they resolve ("destroy that creature", "tap that
     * creature", "that much").
     */
    private void collectDealsDamageToCreatureAbilities(GameData gameData, Permanent watcher, UUID controllerId,
            List<Trigger> watches, boolean readGrants, DealsDamageToCreatureEvent event) {
        if (watcher.isLosesAllAbilitiesUntilEndOfTurn()) {
            return;
        }
        Card card = LayerSystemService.currentCard(gameData, watcher);
        List<List<TriggeredAbility>> abilitiesByWatch = new ArrayList<>(watches.size());
        boolean watchesAny = readGrants;
        for (Trigger watch : watches) {
            Trigger watched = watch;
            List<TriggeredAbility> abilities = watcher.isFaceDown()
                    ? new ArrayList<>()
                    : new ArrayList<>(Read.printed(card, watch));
            abilities.addAll(WatchedAbilities.temporary(watcher, watched));
            abilities.addAll(WatchedAbilities.persistent(watcher, watched));
            watchesAny |= !abilities.isEmpty();
            abilitiesByWatch.add(abilities);
        }
        if (!watchesAny || gameQueryService.hasLostPrintedAbilities(gameData, watcher)) {
            return;
        }
        ConditionContext context = ConditionContext.forPermanent(watcher, controllerId)
                .withTriggeringPermanentId(event.damagedCreatureId()).withEventValue(event.damage());
        TriggerContext triggerContext = new TriggerContext.CreatureDealsDamageToCreature(
                event.damageSource(), event.damagedCreatureId(), event.damage(), event.combatDamage(),
                event.damagedCreature(), event.damagedCreatureControllerId());
        Permanent snapshot = new Permanent(watcher);
        for (int i = 0; i < watches.size(); i++) {
            Trigger watch = watches.get(i);
            Trigger watched = watch;
            List<TriggeredAbility> abilities = abilitiesByWatch.get(i);
            if (readGrants) {
                abilities.addAll(grantedTriggeredAbilitySupport.grantedWatchedAbilities(
                        gameData, watcher, controllerId, watched));
            }
            for (TriggeredAbility ability : abilities) {
                requireEachEvent(ability, watch);
                DamageTrigger trigger = Read.trigger(ability, watched);
                if (trigger.combatOnly() && !event.combatDamage()) continue;
                if (trigger.filter() != null && !damageFilterMatches(gameData, watcher, controllerId,
                        trigger.filter(), event.damageSource())) {
                    continue;
                }
                if (trigger.damagedFilter() != null && !damageFilterMatches(gameData, watcher, controllerId,
                        trigger.damagedFilter(), damagedCreatureOf(gameData, event))) {
                    continue;
                }
                putEventAbility(gameData, ability, watcher.getCard(), watcher, controllerId, context, triggerContext,
                        entry -> {
                            entry.setNonTargeting(true);
                            entry.setEventValue(event.damage());
                            entry.setTriggeringPermanentId(event.damagedCreatureId());
                            entry.setTriggeringPermanentControllerId(event.damagedCreatureControllerId());
                            entry.setEventDamageSource(event.damageSource(), event.damageSourceControllerId());
                            entry.setSourcePermanentSnapshot(snapshot);
                        });
            }
        }
    }

    /** The creature {@code event} dealt damage to, as it was dealt the damage, or as it is now. */
    private Permanent damagedCreatureOf(GameData gameData, DealsDamageToCreatureEvent event) {
        return event.damagedCreature() != null
                ? event.damagedCreature()
                : gameQueryService.findPermanentById(gameData, event.damagedCreatureId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, Permanent host) {
        return isEquippedBy(gameData, equipment, host.getId());
    }

    private boolean isEquippedBy(GameData gameData, Permanent equipment, UUID hostId) {
        if (hostId.equals(equipment.getAttachedTo())) return true;
        // Only a host that already left the battlefield may be matched on last-known attachment.
        return hostId.equals(equipment.getLastAttachedTo())
                && gameQueryService.findPermanentById(gameData, hostId) == null;
    }

    /**
     * Fires ON_ANY_CREATURE_DEALT_DAMAGE triggers on every permanent with that slot, regardless of
     * who controls the damaged creature. Called once per damaged creature.
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    int damageDealt) {
        UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        checkAnyCreatureDealtDamageTriggers(gameData, damagedCreature, damagedCreatureControllerId, damageDealt);
    }

    /**
     * Variant for combat, where the damaged creature may already have left the battlefield by the
     * time its trigger is queued and its controller was captured during damage processing. "Whenever a [filtered]
     * creature is dealt damage, …", whatever the effects are: the record names the damaged creature as its triggering
     * permanent with its controller as it was dealt the damage and its last known information (CR 608.2h), the damage
     * as its event value ("that much") and the source's snapshot, and the creature as the non-targeting "that
     * creature"; the door marks "do this only once each turn" as the action is taken (CR 603.2h). A targeted ability,
     * "you may" or not, chooses its target as it goes on the stack (CR 603.3d; the "you may" is answered as it
     * resolves, CR 603.5).
     */
    public void checkAnyCreatureDealtDamageTriggers(GameData gameData, Permanent damagedCreature,
                                                    UUID damagedCreatureControllerId, int damageDealt) {
        if (damagedCreatureControllerId == null || damageDealt <= 0) return;

        var ctx = new TriggerContext.AnyCreatureDealtDamage(
                damagedCreature, damagedCreatureControllerId, damageDealt);
        Trigger creatureDealtDamage = Triggers.creatureDealtDamage();
        Trigger watched = creatureDealtDamage;
        gameData.forEachPermanent((playerId, perm) -> {
            for (TriggeredAbility ability : abilities(gameData, perm, creatureDealtDamage)) {
                requireEachEvent(ability, creatureDealtDamage);
                // "Whenever a [filtered] creature is dealt damage": the filter is on the damaged creature.
                PermanentPredicate filter = Read.filter(ability, watched);
                if (filter != null && !damageFilterMatches(gameData, perm, playerId, filter, damagedCreature)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId)
                                .withTriggeringPermanentId(damagedCreature.getId()).withEventValue(damageDealt), ctx,
                        damagedPermanentRecord(gameData, perm, damagedCreature, damagedCreatureControllerId,
                                damageDealt, false, true));
            }
        });

        collectTemporaryGlobalTriggers(gameData, Triggers.creatureDealtDamage(),
                damagedCreature.getId(), damageDealt);

        for (DelayedWatchedCreatureDealtDamage watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamage.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) {
                continue;
            }

            StackEntry trigger = TriggeredAbilityStackService.madeAbility(watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    (UUID) null,
                    watch.watchedPermanentId());
            trigger.setNonTargeting(true);
            trigger.setEventValue(damageDealt);
            trigger.setDamageSourceCard(damagedCreature.getCard());
            trigger.setDamageSourceControllerId(
                    gameQueryService.findPermanentController(gameData, damagedCreature.getId()));
            trigger.setSourcePermanentSnapshot(new Permanent(damagedCreature));
            triggeredAbilityStackService.putDelayed(gameData, trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
            log.info("Game {} - {} delayed trigger fires after damage to watched creature",
                    gameData.id, watch.sourceCard().getName());
        }
    }

    /**
     * Fires ON_ANY_PERMANENT_DEALT_DAMAGE triggers once for the damaged permanent: "whenever a permanent you control is
     * dealt damage, …" ("a Dinosaur you control", "this or a creature it's paired with": the trigger's filter on the
     * damaged permanent, the relation judged here). The ability is the watcher's (CR 113.7); its record names the
     * damaged permanent with its power ({@link #damagedPermanentRecord}).
     */
    public void checkAnyPermanentDealtDamageTriggers(GameData gameData, Permanent damagedPermanent,
                                                     int damageDealt) {
        if (damagedPermanent == null || damageDealt <= 0) return;

        UUID damagedPermanentControllerId = gameQueryService.findPermanentController(
                gameData, damagedPermanent.getId());
        if (damagedPermanentControllerId == null) return;

        var ctx = new TriggerContext.AnyPermanentDealtDamage(
                damagedPermanent, damagedPermanentControllerId, damageDealt);
        Trigger watch = Triggers.permanentYouControlDealtDamage();
        Trigger watched = watch;
        gameData.forEachPermanent((playerId, perm) -> {
            // "A permanent you control" (the trigger's relation): the damaged permanent's controller is the watcher's.
            if (!playerId.equals(damagedPermanentControllerId)) return;
            for (TriggeredAbility ability : abilities(gameData, perm, watch)) {
                requireEachEvent(ability, watch);
                // "Whenever a [filtered] permanent you control is dealt damage": the filter is on the damaged permanent.
                PermanentPredicate filter = Read.filter(ability, watched);
                if (filter != null && !damageFilterMatches(gameData, perm, playerId, filter, damagedPermanent)) {
                    continue;
                }
                putEventAbility(gameData, ability, perm.getCard(), perm, playerId,
                        ConditionContext.forPermanent(perm, playerId)
                                .withTriggeringPermanentId(damagedPermanent.getId()).withEventValue(damageDealt), ctx,
                        damagedPermanentRecord(gameData, perm, damagedPermanent, damagedPermanentControllerId,
                                damageDealt, true, false));
            }
        });
    }

    /**
     * Puts each turn-scoped "whenever …" ability registered for the damage or combat-damage trigger {@code watched}
     * (a delayed triggered ability with a stated duration, CR 603.7b) on the stack for the event about
     * {@code targetId}: its filter, if it has one, is judged on that permanent; "a creature you control deals combat
     * damage to a player" only sees a creature its controller controls.
     */
    public void collectTemporaryGlobalTriggers(GameData gameData, Trigger watched, UUID targetId, int eventValue) {
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasWatchedTrigger(watched) || watcher.watchedPermanentId() != null) {
                continue;
            }
            boolean allyCombatDamage = watched.equals(Triggers.creatureYouControlDealsCombatDamageToPlayer());
            if (allyCombatDamage && !watcher.controllerId().equals(
                    gameQueryService.findPermanentController(gameData, targetId))) {
                continue;
            }
            Read read = Read.of(watcher.ability(), watched);
            if (!allyCombatDamage) {
                read.requireNoLimitOrMultiplicity(watched);
                if (read.filter() != null) {
                    Permanent triggeringPermanent = targetId == null ? null
                            : gameQueryService.findPermanentById(gameData, targetId);
                    if (triggeringPermanent == null || !predicateEvaluationService.matchesPermanentPredicate(
                            triggeringPermanent, read.filter(), FilterContext.of(gameData)
                                    .withSourceCardId(watcher.sourceCard().getId())
                                    .withSourceControllerId(watcher.controllerId()))) {
                        continue;
                    }
                }
            }
            enqueueTemporaryGlobalTrigger(gameData, watcher, temporaryEffects(watcher), targetId, eventValue);
        }
    }


    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                UUID targetId, int eventValue) {
        enqueueTemporaryGlobalTrigger(gameData, watcher, temporaryEffects(watcher), targetId, eventValue);
    }

    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                List<CardEffect> effects,
                                                UUID targetId, int eventValue) {
        enqueueTemporaryGlobalTrigger(gameData, watcher, effects, targetId, eventValue, null);
    }

    /**
     * Puts {@code watcher}'s ability, a delayed triggered ability with a stated duration (CR 603.7b), on the stack as
     * {@code effects} for the event about {@code targetId}, the object or player it acts on without targeting it.
     */
    private void enqueueTemporaryGlobalTrigger(GameData gameData,
                                                TemporaryGlobalTriggeredAbility watcher,
                                                List<CardEffect> effects,
                                                UUID targetId, int eventValue, UUID triggeringPermanentId) {
        StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability", effects);
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setLinkedExiledCardId(watcher.linkedExiledCardId());
        if (triggeringPermanentId != null) {
            entry.setTriggeringPermanentId(triggeringPermanentId);
        }
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global {} trigger fires",
                gameData.id, watcher.sourceCard().getName(), LegacySlots.label(watcher.ability()));
    }

    /** The filter context of a predicate {@code watcher}, a permanent with a triggered ability, judges. */
    private static FilterContext watcherFilterContext(GameData gameData, Permanent watcher, UUID watcherControllerId) {
        return FilterContext.of(gameData)
                .withSourceCardId(watcher.getOriginalCard().getId())
                .withSourceControllerId(watcherControllerId)
                .withSourcePermanentId(watcher.getId())
                .withSourcePermanentSnapshot(watcher);
    }

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId) {
        checkNoncombatDamageToOpponentTriggers(gameData, damagedPlayerId, null, 0);
    }

    public void checkNoncombatDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
            UUID sourceControllerId, int damageAmount) {
        var ctx = new TriggerContext.NoncombatDamageToOpponent(
                damagedPlayerId, sourceControllerId, damageAmount);

        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (playerId.equals(damagedPlayerId)) return;

            for (Permanent perm : List.copyOf(battlefield)) {
                putPlayerDamageAbilities(gameData, perm, playerId, Triggers.opponentDealtNoncombatDamage(),
                        ctx, damagedPlayerId, damagedPlayerId, damageAmount, null, sourceControllerId);
            }
        });

        if (sourceControllerId == null || sourceControllerId.equals(damagedPlayerId)) return;

        putGraveyardDamageAbilities(gameData, sourceControllerId,
                Triggers.sourceYouControlDealsNoncombatDamageToOpponent(), ctx);
    }

    /**
     * Hands each ability {@code watch} reads of the cards in {@code controllerId}'s graveyard (their own and their
     * perpetual ones; none while cards in graveyards have lost all abilities) to the stack door for {@code event}, the
     * record naming this graveyard incarnation of the card (CR 400.7). No graveyard damage ability is written with a
     * filter or a multiplicity; this throws on one.
     */
    private void putGraveyardDamageAbilities(GameData gameData, UUID controllerId, Trigger watch,
                                             TriggerContext event) {
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null) return;

        for (Card card : new ArrayList<>(graveyard)) {
            for (TriggeredAbility ability : WatchedAbilities.withWatchedTrigger(
                    triggerIndex.graveyardAbilities(gameData, card), watch,
                    Zone.GRAVEYARD)) {
                requireNothingToJudge(ability, watch);
                long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
                putEventAbility(gameData, ability, card, null, controllerId, ConditionContext.forCard(card, controllerId),
                        event, entry -> entry.setSourceGraveyardEntryVersion(graveyardEntryVersion));
            }
        }
    }

    /**
     * Chandra's Phoenix: "Whenever an opponent is dealt damage by a red instant or sorcery spell you
     * control or by a red planeswalker you control, return this card from your graveyard to your hand."
     *
     * <p>{@code entry} is the stack entry that dealt the damage. It qualifies when it is a red instant
     * or sorcery spell, or an ability whose source card is a red planeswalker. The graveyard scanned is
     * the entry controller's ("you control"), and the damaged player must be one of their opponents.
     */
    public void checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(GameData gameData, UUID damagedPlayerId,
                                                                    StackEntry entry) {
        if (entry == null) return;

        UUID controllerId = entry.getControllerId();
        if (controllerId == null || controllerId.equals(damagedPlayerId)) return;

        Card sourceCard = entry.getEffectiveDamageSourceCard();
        if (sourceCard == null) return;
        // An ability's source permanent is judged by its current characteristics (a planeswalker
        // made blue by Prismatic Lace isn't red), falling back to last-known information.
        Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (sourcePermanent == null) {
            sourcePermanent = entry.getSourcePermanentSnapshot();
        }
        boolean red = sourcePermanent != null
                ? gameQueryService.getEffectiveColors(gameData, sourcePermanent).contains(CardColor.RED)
                : sourceCard.getColors().contains(CardColor.RED);
        if (!red) return;

        boolean redSpell = entry.getEntryType() == StackEntryType.INSTANT_SPELL
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL;
        boolean redPlaneswalker = sourcePermanent != null
                ? gameQueryService.isPlaneswalker(gameData, sourcePermanent)
                : sourceCard.hasType(CardType.PLANESWALKER);
        if (!redSpell && !redPlaneswalker) return;

        putGraveyardDamageAbilities(gameData, controllerId,
                Triggers.opponentDealtDamageByRedInstantSorceryOrPlaneswalkerYouControl(),
                new TriggerContext.DamageToControllerAmount(damagedPlayerId, 0));
    }

    /**
     * Bloodfeather Phoenix: "Whenever an instant or sorcery spell you control deals damage to an
     * opponent or battle". The caller has already batched the spell's damage event and supplies
     * whether that event hit a qualifying recipient.
     */
    public void checkInstantOrSorceryDamageToOpponentOrBattleTriggers(GameData gameData,
                                                                       Card sourceCard,
                                                                       UUID sourceControllerId,
                                                                       boolean damagedOpponentOrBattle) {
        if (!damagedOpponentOrBattle || sourceCard == null || sourceControllerId == null
                || (!sourceCard.hasType(CardType.INSTANT) && !sourceCard.hasType(CardType.SORCERY))) {
            return;
        }
        putGraveyardDamageAbilities(gameData, sourceControllerId,
                Triggers.instantOrSorceryYouControlDealsDamageToOpponentOrBattle(),
                new TriggerContext.SourceDealsDamage(sourceCard, sourceControllerId, null, 0, Map.of(), null,
                        Map.of(), false));
    }

    /** Handles "Whenever damage that would be dealt to you is prevented, ..." triggers: the damage prevented is "that much". */
    public void checkControllerDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId, int amount) {
        if (amount <= 0) return;

        List<Permanent> damagedPlayerBattlefield = gameData.playerBattlefields.get(damagedPlayerId);
        if (damagedPlayerBattlefield == null) return;

        var ctx = new TriggerContext.DamageToControllerAmount(damagedPlayerId, amount, null, null);
        Trigger watch = Triggers.damageToYouPrevented();
        for (Permanent perm : List.copyOf(damagedPlayerBattlefield)) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, perm, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, perm.getCard(), perm, damagedPlayerId,
                        ConditionContext.forPermanent(perm, damagedPlayerId).withEventValue(amount), ctx,
                        entry -> entry.setEventValue(amount));
            }
        }
    }

    /**
     * The effects of {@code watcher}'s ability, every one of them as written (CR 603.3), its intervening "if" wrapped
     * around each to be judged again as it resolves (CR 603.4).
     */
    private static List<CardEffect> temporaryEffects(TemporaryGlobalTriggeredAbility watcher) {
        Condition interveningIf = watcher.ability().options().interveningIf();
        return watcher.ability().effects().stream()
                .map(EffectRegistration::effect)
                .map(effect -> interveningIf == null ? effect : (CardEffect) new ConditionalEffect(interveningIf, effect))
                .toList();
    }

    /**
     * "Whenever a creature you control deals combat damage to a player" of the face-up planes, for {@code creature}'s
     * combat damage: each ability, its filter judged on the creature and its event condition judged, goes to the stack
     * door whole, one object (CR 603.3), its source the plane, which judges its intervening "if"; "one or more" triggers
     * once in {@code firedBatchedAbilities}' step. The entry records the damaged player as its non-targeting "that
     * player", the damage as its event value, and, for each creature's ability, the creature as it dealt the damage.
     */
    public void checkPlanarAllyCreatureCombatDamageToPlayerTriggers(
            GameData gameData, Permanent creature, UUID attackerId, UUID defenderId, int damageDealt,
            Set<String> firedBatchedAbilities) {
        if (damageDealt <= 0 || gameData.planechase == null || gameData.planechase.controllerId == null
                || creature == null || !gameData.planechase.controllerId.equals(attackerId)) {
            return;
        }

        UUID planarControllerId = gameData.planechase.controllerId;
        Trigger watched = Triggers.creatureYouControlDealsCombatDamageToPlayer();
        FilterContext triggerContext = FilterContext.of(gameData)
                .withSourceControllerId(planarControllerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            List<TriggeredAbility> abilities = WatchedAbilities.printed(object.getCard(), Triggers.creatureYouControlDealsCombatDamageToPlayer(), Zone.BATTLEFIELD);
            ConditionContext context = ConditionContext.forCard(object.getCard(), planarControllerId);
            for (int abilityIndex = 0; abilityIndex < abilities.size(); abilityIndex++) {
                TriggeredAbility ability = abilities.get(abilityIndex);
                TriggerMultiplicity multiplicity = ability.options().multiplicity();
                if (ability.options().limit() != TriggerLimit.NONE
                        || multiplicity == TriggerMultiplicity.EACH_BLOCKER) {
                    throw new IllegalStateException("The planar combat damage collector reads no "
                            + ability.options() + ": " + ability);
                }
                PermanentPredicate filter = Read.filter(ability, watched);
                if (filter != null
                        && !predicateEvaluationService.matchesPermanentPredicate(creature, filter, triggerContext)) {
                    continue;
                }
                if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
                    continue;
                }
                boolean oneOrMore = multiplicity == TriggerMultiplicity.ONE_OR_MORE;
                if (oneOrMore && firedBatchedAbilities.contains(object.getId() + ":" + abilityIndex)) {
                    continue;
                }
                PlanarObject plane = object.copy();
                boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                        object.getCard(), null, planarControllerId,
                        object.getCard().getName() + "'s triggered ability", context, entry -> {
                            entry.setTargetId(defenderId);
                            if (!oneOrMore) {
                                entry.setTriggeringPermanentId(creature.getId());
                                entry.setTriggeringPermanentControllerId(attackerId);
                                entry.setTriggeringPermanent(creature);
                            }
                            entry.setEventValue(damageDealt);
                            entry.setSourcePlanarObject(plane);
                            entry.setNonTargeting(true);
                        }));
                if (triggered) {
                    if (oneOrMore) {
                        firedBatchedAbilities.add(object.getId() + ":" + abilityIndex);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(object.getCard()));
                }
            }
        }
    }

    private boolean hasPlanarPermanentDamageToYouTrigger(GameData gameData, UUID damagedPlayerId) {
        return gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                && gameData.planechase.faceUp.stream()
                .anyMatch(object -> !Read.printed(object.getCard(), Triggers.permanentDealsDamageToYou()).isEmpty());
    }

    private void checkPlanarPermanentDealsDamageToYouTriggers(
            GameData gameData, UUID damagedPlayerId, Permanent damageSource, boolean isCombatDamage) {
        if (!gameQueryService.isCreature(gameData, damageSource)
                || gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, damagedPlayerId)) {
            return;
        }

        TriggerContext context = new TriggerContext.DamageToController(
                damagedPlayerId, damageSource.getId(), isCombatDamage);
        Trigger watch = Triggers.permanentDealsDamageToYou();
        UUID creatureControllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            // Each ability of the plane, whole (CR 603.3; Stensia's "put a +1/+1 counter on it"): the plane its source
            // and the creature that dealt the damage its triggering permanent; "once each turn for each creature" is
            // the door's, by the creature.
            for (TriggeredAbility ability : Read.printed(object.getCard(), watch)) {
                requireNothingToJudge(ability, watch);
                PlanarObject plane = object.copy();
                putEventAbility(gameData, ability, object.getCard(), null, damagedPlayerId,
                        ConditionContext.forCard(object.getCard(), damagedPlayerId)
                                .withTriggeringPermanentId(damageSource.getId()), context, entry -> {
                            entry.setTriggeringPermanentId(damageSource.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setSourcePlanarObject(plane);
                            entry.setNonTargeting(true);
                        }, damageSource.getId());
            }
        }
    }

    private void checkPlanarAllySourceDealtDamageToOpponentTriggers(
            GameData gameData, UUID damagedPlayerId, UUID sourceControllerId,
            UUID sourcePermanentId, int amount) {
        if (gameData.planechase == null
                || !Objects.equals(gameData.planechase.controllerId, sourceControllerId)
                || sourcePermanentId == null) {
            return;
        }

        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.DamageToControllerAmount(
                damagedPlayerId, amount, sourcePermanentId, sourceControllerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarDamageAbilities(gameData, object, Triggers.sourceYouControlDealsDamageToOpponent(), context,
                    sourceControllerId, damagedPlayerId, amount, sourcePermanentId, sourceControllerId);
        }
    }

    private void checkPlanarSourceDealtDamageToItsControllerTriggers(
            GameData gameData, UUID damagedPlayerId, UUID sourcePermanentId, int amount) {
        if (gameData.planechase == null
                || gameData.planechase.controllerId == null
                || Objects.equals(gameData.planechase.controllerId, damagedPlayerId)
                || sourcePermanentId == null) {
            return;
        }

        Permanent damageSource = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        if (damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) return;

        TriggerContext context = new TriggerContext.DamageToControllerAmount(
                damagedPlayerId, amount, sourcePermanentId, damagedPlayerId);
        for (PlanarObject object : List.copyOf(gameData.planechase.faceUp)) {
            putPlanarDamageAbilities(gameData, object, Triggers.creatureDealsDamageToItsController(),
                    context, gameData.planechase.controllerId, damagedPlayerId, amount, sourcePermanentId,
                    damagedPlayerId);
        }
    }

    /**
     * Hands each damage ability {@code watch} reads of the face-up plane {@code object} to the stack door under
     * {@code controllerId}, whole (CR 603.3), recording the damage to the player ({@link #playerDamageRecord}, the plane
     * as the ability's source). "Once each turn for each creature" (Stensia) is the door's, by the creature that dealt
     * the damage.
     */
    private void putPlanarDamageAbilities(GameData gameData, PlanarObject object, Trigger watch,
                                          TriggerContext context, UUID controllerId, UUID damagedPlayerId, int amount,
                                          UUID sourcePermanentId, UUID sourceControllerId) {
        for (TriggeredAbility ability : Read.printed(object.getCard(), watch)) {
            requireNothingToJudge(ability, watch);
            putEventAbility(gameData, ability, object.getCard(), null, controllerId,
                    ConditionContext.forCard(object.getCard(), controllerId).withTargetId(damagedPlayerId)
                            .withTriggeringPermanentId(sourcePermanentId).withEventValue(amount), context,
                    playerDamageRecord(null, object, damagedPlayerId, damagedPlayerId, amount, sourcePermanentId,
                            sourceControllerId), sourcePermanentId);
        }
    }

    /**
     * Hands {@code ability}, a triggered ability of {@code sourceCard} ({@code source} on the battlefield, or
     * {@code null} for a card in another zone) controlled by {@code controllerId}, to the stack door for
     * {@code event}: its event condition is judged now in {@code context} (CR 603.2), which carries what the event
     * names (its value, "that player", the triggering permanent); the door judges its intervening "if" in the same
     * context (CR 603.4) and its limit, counts the additional times it triggers, stamps {@code record} on its entry
     * whatever its effects are and chooses its targets as it goes on the stack (CR 603.3d).
     *
     * @return whether it triggered
     */
    private boolean putEventAbility(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                                    UUID controllerId, ConditionContext context, TriggerContext event,
                                    TriggerRecord record) {
        return putEventAbility(gameData, ability, sourceCard, source, controllerId, context, event, record, null);
    }

    /**
     * {@link #putEventAbility(GameData, TriggeredAbility, Card, Permanent, UUID, ConditionContext, TriggerContext,
     * TriggerRecord)} for an event about the creature {@code limitCreatureId}, by which "once each turn for each
     * creature" (Stensia) is judged and marked.
     */
    private boolean putEventAbility(GameData gameData, TriggeredAbility ability, Card sourceCard, Permanent source,
                                    UUID controllerId, ConditionContext context, TriggerContext event,
                                    TriggerRecord record, UUID limitCreatureId) {
        if (!conditionEvaluationService.eventConditionMet(gameData, ability.options(), context)) {
            return false;
        }
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(ability,
                sourceCard, source, controllerId, sourceCard.getName() + "'s ability", context, record)
                .withEvent(event).withLimitCreature(limitCreatureId));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
        }
        return triggered;
    }

    /**
     * The abilities of {@code permanent} with a condition watched as {@code watched}, each to be handed whole to the
     * stack door, as the layers leave them ({@link TriggerIndex#permanentAbilities}: none of its card's while it is face
     * down or has lost them, CR 708.2, 613.1f; those granted it; those other permanents' static abilities give it), its
     * card's read through their text changes (Layer 3, CR 612.1: Glamerdye on Riverfall Mimic).
     */
    List<TriggeredAbility> abilities(GameData gameData, Permanent permanent, Trigger watched) {
        Card card = LayerSystemService.currentCard(gameData, permanent);
        List<TriggeredAbility> printed = card.getTriggeredAbilities();
        List<TriggeredAbility> abilities = new ArrayList<>();
        for (TriggeredAbility ability : triggerIndex.permanentAbilities(gameData, permanent)) {
            if (!ability.hasWatchedTrigger(watched, Zone.BATTLEFIELD)) {
                continue;
            }
            boolean ofTheCard = printed.stream().anyMatch(own -> own == ability);
            abilities.add(ofTheCard ? textChanged(gameData, permanent, ability) : ability);
        }
        return abilities;
    }

    private List<TriggeredAbility> printedWatchedAbilities(GameData gameData, Permanent perm, Trigger watch) {
        return abilities(gameData, perm, watch);
    }

    /** {@code ability} of {@code perm} read through its text changes (Layer 3, CR 612.1). */
    private static TriggeredAbility textChanged(GameData gameData, Permanent perm, TriggeredAbility ability) {
        List<TextReplacement> globalWordChange = TextChangeTransformer.globalColorWordReplacements(gameData);
        if (perm.getTextReplacements().isEmpty() && globalWordChange.isEmpty()) {
            return ability;
        }
        List<EffectRegistration> transformed = new ArrayList<>(ability.effects().size());
        boolean changed = false;
        for (EffectRegistration registration : ability.effects()) {
            CardEffect effect = TextChangeTransformer.transform(registration.effect(), perm.getTextReplacements(),
                    globalWordChange);
            changed |= effect != registration.effect();
            transformed.add(new EffectRegistration(effect, registration.simultaneousWithPrevious()));
        }
        return changed ? ability.withEffects(transformed) : ability;
    }

    /**
     * The effects each of {@code abilities} puts on the stack, one list per ability that has any, as {@code read}
     * reads an ability's: the flat readers' list of one ability.
     */
    private static List<CardEffect> effectsOf(List<EffectRegistration> registrations) {
        return registrations.stream().map(EffectRegistration::effect).toList();
    }

    /**
     * "Whenever a source you control deals noncombat damage to a creature": the watchers are the source's controller's
     * permanents, the damaged permanent a creature. Each ability's event condition is judged as the damage is dealt,
     * and its intervening "if" by the door (CR 603.4: Crude Abattoir's "if the second door is unlocked"), with the
     * damage as the event's value and the creature as the triggering permanent (Taii Wakeen's "equal to that
     * creature's toughness"). The record names the creature ({@link #damagedPermanentRecord}).
     */
    public void checkAllySourceDealtNoncombatDamageToCreatureTriggers(
            GameData gameData, UUID sourceControllerId, Permanent damagedCreature, int damageDealt) {
        if (sourceControllerId == null || damagedCreature == null || damageDealt <= 0
                || !gameQueryService.isCreature(gameData, damagedCreature)) return;
        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return;

        TriggerContext context = new TriggerContext.SourceDealsNoncombatDamageToCreature(
                damagedCreature, damageDealt, sourceControllerId);
        UUID damagedControllerId = gameQueryService.findPermanentController(gameData, damagedCreature.getId());
        Trigger watch = Triggers.sourceYouControlDealsNoncombatDamageToCreature();
        for (Permanent watcher : List.copyOf(battlefield)) {
            for (TriggeredAbility ability : printedWatchedAbilities(gameData, watcher, watch)) {
                requireNothingToJudge(ability, watch);
                putEventAbility(gameData, ability, watcher.getCard(), watcher, sourceControllerId,
                        ConditionContext.forPermanent(watcher, sourceControllerId)
                                .withTriggeringPermanentId(damagedCreature.getId()).withEventValue(damageDealt),
                        context, damagedPermanentRecord(gameData, watcher, damagedCreature, damagedControllerId,
                                damageDealt, false, false));
            }
        }
    }

    /** Throws when {@code ability} has a filter the collector of {@code watch} doesn't judge, or a multiplicity. */
    private static void requireNothingToJudge(TriggeredAbility ability, Trigger watch) {
        requireEachEvent(ability, watch);
        if (Read.filter(ability, watch) != null) {
            throw new IllegalStateException("The collector of " + watch + " judges no filter: " + ability);
        }
    }

    /**
     * Throws when {@code ability} triggers other than once for each event its collector reports ("once for each
     * source", CR 603.2c, which only the combat damage step's collector reads).
     */
    private static void requireEachEvent(TriggeredAbility ability, Trigger watch) {
        if (ability.options().multiplicity() != TriggerMultiplicity.EACH) {
            throw new IllegalStateException("The collector of " + watch + " reads no "
                    + ability.options().multiplicity() + ": " + ability);
        }
    }

    /**
     * The emblems' "whenever a creature you control deals combat damage to a player" abilities
     * ({@code Triggers.creatureYouControlDealsCombatDamageToPlayer()}; Kaito Shizuki's emblem) for {@code creature},
     * which {@code creatureControllerId} controls, dealing {@code damageDealt} combat damage to {@code damagedPlayerId}.
     * Abilities of emblems function in the command zone (CR 114.4). Each one goes on the stack once as one object of its
     * effects (CR 603.3), the emblem's source card as its source, and records the event whatever its effects are: the
     * creature as its triggering permanent, the player dealt damage as its triggering player and the damage as its
     * event value. Nothing makes an emblem's ability trigger additional times (CR 110.1: an emblem isn't a permanent);
     * the reader judges no filter or options, and an ability with one fails loudly instead of triggering wrongly.
     */
    public void checkEmblemAllyCreatureCombatDamageToPlayerTriggers(GameData gameData, Permanent creature,
                                                                     UUID creatureControllerId, UUID damagedPlayerId,
                                                                     int damageDealt) {
        if (creature == null || creatureControllerId == null || damageDealt <= 0) {
            return;
        }
        Trigger watched = Triggers.creatureYouControlDealsCombatDamageToPlayer();
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!emblem.controllerId().equals(creatureControllerId)) {
                continue;
            }
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (ability.conditions().stream().noneMatch(condition -> condition.trigger().equals(watched))) {
                    continue;
                }
                if (!ability.options().equals(TriggerOptions.DEFAULT)) {
                    throw new IllegalStateException(emblemName(emblem) + " has a combat damage ability its reader "
                            + "doesn't judge: " + ability);
                }
                Card source = emblem.sourceCard() != null ? emblem.sourceCard() : creature.getCard();
                String description = emblemName(emblem);
                putEmblemAbility(gameData, emblem, ability, source, description,
                        ConditionContext.forCard(source, emblem.controllerId()), entry -> {
                            entry.setTriggeringPermanentId(creature.getId());
                            entry.setTriggeringPermanentControllerId(creatureControllerId);
                            entry.setTriggeringPlayerId(damagedPlayerId);
                            entry.setEventValue(damageDealt);
                            entry.setNonTargeting(true);
                        }, false);
                gameLogService.append(gameData, GameLog.text(description + " triggers."));
                log.info("Game {} - {} triggers for combat damage dealt to a player", gameData.id, description);
            }
        }
    }

    /**
     * Fires delayed triggers watching a Wall for damage from an attacking creature. The source's
     * attacking status is checked at the damage event, so noncombat damage from a spell and damage
     * from a blocking creature do not qualify.
     */
    public void checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
            GameData gameData, Permanent damageSource, Permanent damagedCreature, int damage) {
        if (damageSource == null || damagedCreature == null || damage <= 0
                || !damageSource.isAttacking() || !gameQueryService.isCreature(gameData, damageSource)) {
            return;
        }

        for (DelayedWatchedCreatureDealtDamageByAttackingCreature watch
                : gameData.getDelayedActions(DelayedWatchedCreatureDealtDamageByAttackingCreature.class)) {
            if (!watch.watchedPermanentId().equals(damagedCreature.getId())) continue;

            StackEntry trigger = TriggeredAbilityStackService.madeAbility(watch.sourceCard(),
                    watch.controllerId(),
                    watch.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(watch.effects()),
                    watch.watchedPermanentId(),
                    (UUID) null);
            trigger.setNonTargeting(true);
            trigger.setEventValue(damage);
            triggeredAbilityStackService.putDelayed(gameData, trigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(watch.sourceCard()));
        }
    }

    private static String emblemName(Emblem emblem) {
        return (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem") + "'s emblem";
    }

    /**
     * Puts {@code ability} of {@code emblem} on the stack through the stack door: an ability of an emblem functions in
     * the command zone (CR 114.4), {@code source} its source, and it records the event with {@code record}. Its targets
     * are only those its effects or its own target filter declare: no ability of the card that made the emblem is it.
     */
    private void putEmblemAbility(GameData gameData, Emblem emblem, TriggeredAbility ability, Card source,
                                  String description, ConditionContext ifContext, TriggerRecord record,
                                  boolean creatureDying) {
        TriggeredAbilityRequest request = new TriggeredAbilityRequest(ability, source, null, emblem.controllerId(),
                description, ifContext, entry -> {
                    entry.setMadeByResolution(true);
                    record.stamp(entry);
                }).ofEmblem();
        triggeredAbilityStackService.put(gameData, creatureDying ? request.withCreatureDying() : request);
    }

    /**
     * The emblems' "whenever an opponent is dealt combat damage by one or more creatures you control" abilities
     * ({@code Triggers.creaturesYouControlDealCombatDamageToOpponent()}; Lolth, Spider Queen's emblem) for the combat
     * damage {@code controllerId}'s creatures dealt {@code damagedPlayerId} in one combat damage step. Abilities of
     * emblems function in the command zone (CR 114.4). Each ability whose intervening "if" holds as it triggers (CR
     * 603.4: "if that player lost less than 8 life this turn", judged with that player as the context's target) goes
     * on the stack once as one object of its effects (CR 603.3), the emblem's source card as its source, "that player"
     * its non-targeting {@code targetId} and triggering player, the combat damage dealt to them its event value; the
     * intervening "if" wraps each effect, so it is checked again as the ability resolves. Nothing makes an emblem's
     * ability trigger additional times; an ability takes no limit, multiplicity or filter here.
     */
    public void checkEmblemCombatDamageTriggers(GameData gameData, UUID controllerId,
                                                UUID damagedPlayerId,
                                                Map<Permanent, Integer> combatDamageDealtToPlayer) {
        if (controllerId == null || damagedPlayerId == null || controllerId.equals(damagedPlayerId)
                || combatDamageDealtToPlayer == null
                || combatDamageDealtToPlayer.values().stream().noneMatch(damage -> damage > 0)) {
            return;
        }

        Card fallbackSource = combatDamageDealtToPlayer.entrySet().stream()
                .filter(entry -> entry.getValue() > 0)
                .map(entry -> entry.getKey().getCard())
                .findFirst()
                .orElse(null);
        int damage = combatDamageDealtToPlayer.values().stream().filter(amount -> amount > 0)
                .mapToInt(Integer::intValue).sum();
        Trigger watched = Triggers.creaturesYouControlDealCombatDamageToOpponent();

        for (Emblem emblem : gameData.emblems) {
            if (!controllerId.equals(emblem.controllerId())) continue;

            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (ability.conditions().stream().noneMatch(condition -> condition.trigger().equals(watched))) {
                    continue;
                }
                TriggerOptions options = ability.options();
                if (options.limit() != TriggerLimit.NONE || options.multiplicity() != TriggerMultiplicity.EACH) {
                    throw new IllegalStateException("The emblem combat damage reader judges no limit or "
                            + "multiplicity: " + ability);
                }
                Card source = emblem.sourceCard() != null ? emblem.sourceCard() : fallbackSource;
                if (source == null) continue;
                if (!conditionEvaluationService.triggerConditionsMet(gameData, options,
                        ConditionContext.forCard(source, controllerId).withTargetId(damagedPlayerId), damage)) {
                    continue;
                }

                String description = (emblem.sourceCard() != null ? emblem.sourceCard().getName() : "Emblem")
                        + "'s emblem";
                // The stack door judges the intervening "if" again as it triggers and wraps each effect in it, so it
                // is checked again as the ability resolves (CR 603.4).
                putEmblemAbility(gameData, emblem, ability, source, description,
                        ConditionContext.forCard(source, controllerId).withTargetId(damagedPlayerId)
                                .withEventValue(damage), entry -> {
                            entry.setTargetId(damagedPlayerId);
                            entry.setNonTargeting(true);
                            entry.setTriggeringPlayerId(damagedPlayerId);
                            entry.setEventValue(damage);
                        }, false);
                gameLogService.append(gameData, GameLog.text(
                        description + " triggers for combat damage dealt to an opponent."));
                log.info("Game {} - {} triggers for combat damage dealt to an opponent",
                        gameData.id, description);
            }
        }
    }
}
