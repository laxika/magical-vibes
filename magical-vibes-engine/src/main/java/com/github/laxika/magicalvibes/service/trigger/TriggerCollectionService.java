package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Boon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.CreatureEntersTriggerWatcher;
import com.github.laxika.magicalvibes.model.DamagedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.DelayedEffectOnDeath;
import com.github.laxika.magicalvibes.model.DelayedReturnOnDeath;
import com.github.laxika.magicalvibes.model.DelayedSpellCastTrigger;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.LegacySlots;
import com.github.laxika.magicalvibes.model.ManaCopyRider;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.OpeningHandRevealTrigger;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SacrificeBoonWatcher;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetedCreatureDeathTriggerWatcher;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.DelayedControllerSpellCastTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedDestroyTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedExileReturnCounterTrigger;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeSourceWhenTargetLeaves;
import com.github.laxika.magicalvibes.model.action.DelayedSacrificeTargetWhenSourceLeaves;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.condition.TriggeringPermanentHasGreaterPowerOrToughnessThanSource;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.effect.CommanderCreaturesDealPowerDamageToPreventedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.CopyControllerCastSpellEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextSpellCastThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.CopyTriggeringSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DemonstrateEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLinkedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EvolveEffect;
import com.github.laxika.magicalvibes.model.effect.GrantActivatedAbilityEffect;
import com.github.laxika.magicalvibes.model.effect.GrantCastTriggerToSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.IncrementTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.NextMatchingSpellCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PutVoyageCounterOnExiledCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnVoyagingCardFromExileEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerGrant;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardHasXInManaCostPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSpecificPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.rules.CounterChangeEvent;
import com.github.laxika.magicalvibes.model.rules.DepartedAttachments;
import com.github.laxika.magicalvibes.model.rules.RulesEvent;
import com.github.laxika.magicalvibes.model.rules.RulesEventBatch;
import com.github.laxika.magicalvibes.model.rules.SpellEvent;
import com.github.laxika.magicalvibes.model.rules.ZoneChangeEvent;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerCondition;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.model.trigger.ZoneChangeTrigger;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.TriggeredAbilityQueueService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.SpellTriggers;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest.TriggerRecord;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggerCause;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityRequest;
import com.github.laxika.magicalvibes.service.trigger.pipeline.TriggeredAbilityStackService;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * The triggered abilities the trigger pipeline doesn't match yet: the abilities the engine makes for keywords and rules
 * as a spell is cast or an ability activated, and the delayed, temporary, emblem and boon abilities that watch zone
 * changes, each handed to the stack door; and the rules events of becoming the target, choosing targets, playing a
 * land and equipment becoming attached.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TriggerCollectionService {

    /** A record that names nothing beyond what the request's own fields name. */
    private static final TriggerRecord NO_EXTRA_RECORD = entry -> {
    };

    /** Prowess: "Whenever you cast a noncreature spell, this creature gets +1/+1 until end of turn" (CR 702.108a). */
    private static final TriggeredAbility PROWESS_ABILITY = new TriggeredAbility(Triggers.youCastSpell(),
            new com.github.laxika.magicalvibes.model.effect.BoostSelfEffect(1, 1));

    /**
     * Increment: "Whenever you cast a spell, if the amount of mana you spent to cast it is greater than this creature's
     * power or toughness, put a +1/+1 counter on it."
     */
    private static final TriggeredAbility INCREMENT_ABILITY = new TriggeredAbility(Triggers.youCastSpell(),
            new IncrementTriggerEffect());

    private final PlayerInputService playerInputService;

    private final TriggeredAbilityQueueService triggeredAbilityQueueService;

    private final GameQueryService gameQueryService;

    private final PredicateEvaluationService predicateEvaluationService;

    private final TargetLegalityService targetLegalityService;

    private final ConditionEvaluationService conditionEvaluationService;

    private final GameLogService gameLogService;

    @Autowired
    @Lazy
    private TriggeredAbilityStackService triggeredAbilityStackService;

    @Autowired
    private ObjectProvider<VisualCueService> visualCueService;

    @Autowired
    @Lazy
    private SpellTriggers spellTriggers;

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, true);
    }

    /**
     * Fires delayed "until end of turn, whenever you cast a [filter] spell, …" triggers registered
     * this turn (Mountain Titan). One trigger per registration. Registrations that require their
     * source permanent to remain on the battlefield are skipped once it has left.
     */
    private void processDelayedControllerSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        if (!gameData.hasDelayedAction(DelayedControllerSpellCastTrigger.class)) {
            return;
        }
        for (DelayedControllerSpellCastTrigger delayed
                : gameData.getDelayedActions(DelayedControllerSpellCastTrigger.class)) {
            if (!delayed.controllerId().equals(castingPlayerId)) continue;
            Permanent source = delayed.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, delayed.sourcePermanentId());
            if (delayed.sourceMustRemainOnBattlefield() && source == null) {
                continue;
            }
            StackEntry spellEntry = gameData.stack.stream()
                    .filter(candidate -> spellCard.getId().equals(candidate.getCard().getId()))
                    .findFirst()
                    .orElse(null);
            // A spell with {X} on the stack is evaluated with its chosen X (CR 202.3e).
            Integer spellXValue = spellEntry != null && spellCard.getParsedManaCost() != null
                    && spellCard.getParsedManaCost().hasX() ? spellEntry.getXValue() : null;
            if (delayed.spellFilter() != null
                    && !predicateEvaluationService.matchesCardPredicate(
                            spellCard, delayed.spellFilter(), null, gameData, castingPlayerId,
                            null, null, spellXValue)) {
                continue;
            }
            if (delayed.stackEntryFilter() != null
                    && (spellEntry == null
                    || !targetLegalityService.matchesStackEntryPredicate(
                    gameData, spellEntry, delayed.stackEntryFilter(), castingPlayerId,
                    source != null ? source : delayed.sourcePermanentSnapshot(), null,
                    source == null ? delayed.sourcePowerAtLastKnown() : null))) {
                continue;
            }

            if (delayed.oneShot()) {
                gameData.delayedActions.remove(delayed);
            }

            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(), delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger", delayed.resolvedEffects(),
                    (UUID) null, delayed.sourcePermanentId());
            entry.setTriggeringCardId(spellCard.getId());
            entry.setTriggeringSpell(spellEntry);
            if (spellEntry != null) {
                entry.setXValue(spellEntry.getXValue());
            }
            entry.setEventValue(spellManaValue(gameData, spellCard));
            entry.setSourcePermanentSnapshot(delayed.sourcePermanentSnapshot());
            entry.setNonTargeting(true);

            if (delayed.targetFilter() != null) {
                // Its target, restricted by the registration's filter, is chosen as it goes on the stack (CR 603.3d).
                entry.setTargetFilter(delayed.targetFilter());
                triggeredAbilityStackService.putMade(gameData, entry);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
                log.info("Game {} - {} delayed spell-cast trigger awaits a target for {}",
                        gameData.id, delayed.sourceCard().getName(), spellCard.getName());
                continue;
            }

            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.cardTextCard(
                    delayed.sourceCard(), "'s delayed trigger fires for ", spellCard, "."));
            log.info("Game {} - {} delayed spell-cast trigger fires for {}",
                    gameData.id, delayed.sourceCard().getName(), spellCard.getName());
        }
    }

    /**
     * Uses up the loyalty-ability half of the delayed ability {@code link} names, whose instant-or-sorcery half just
     * triggered ("copy the next instant or sorcery spell you cast or loyalty ability you activate this turn").
     */
    private static void removeLinkedLoyaltyCopies(GameData gameData, UUID playerId, UUID link) {
        List<GameData.PendingCopySource> sources = gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.get(playerId);
        if (sources == null) {
            return;
        }
        int before = sources.size();
        sources.removeIf(source -> link.equals(source.link()));
        int removed = before - sources.size();
        if (removed > 0) {
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.computeIfPresent(playerId,
                    (ignored, count) -> count - removed > 0 ? count - removed : null);
        }
        if (sources.isEmpty()) {
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(playerId);
        }
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, boolean castFromHand) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId,
                castFromHand ? Zone.HAND : Zone.GRAVEYARD);
    }

    /**
     * Zone-carrying form: {@code castZone} is the zone the spell was cast from, so triggers that
     * care about a specific origin (cast from a graveyard, cast from the top of a library) can tell
     * them apart instead of sharing one "not from hand" flag.
     */
    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, castZone, null);
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                       Zone castZone, UUID exiledSourcePermanentId) {
        checkSpellCastTriggers(gameData, spellCard, castingPlayerId, castZone, exiledSourcePermanentId, false);
    }

    /**
     * Emits the cosmetic {@link VisualCueType#SPELL_CAST} cue for a spell that was just put on the
     * stack. Targets are public once chosen, so the cue names the first player, the first permanent
     * and the first spell the spell targets; a face-down spell keeps its name and colors hidden.
     */
    private void emitSpellCastCue(GameData gameData, Card spellCard) {
        VisualCueService cues = visualCueService == null ? null : visualCueService.getIfAvailable();
        if (cues == null) {
            return;
        }
        StackEntry spell = null;
        for (StackEntry entry : gameData.stack) {
            if (entry.getCard() != null && entry.getCard().getId().equals(spellCard.getId()) && !entry.isCopy()) {
                spell = entry;
            }
        }
        if (spell == null) {
            return;
        }
        List<UUID> targetIds = new ArrayList<>();
        if (spell.getTargetId() != null) {
            targetIds.add(spell.getTargetId());
        }
        if (spell.getTargetIds() != null) {
            targetIds.addAll(spell.getTargetIds());
        }
        UUID targetPlayerId = null;
        UUID targetPermanentId = null;
        UUID targetCardId = null;
        for (UUID targetId : targetIds) {
            if (gameData.playerIds.contains(targetId)) {
                targetPlayerId = targetPlayerId != null ? targetPlayerId : targetId;
            } else if (gameQueryService.findPermanentById(gameData, targetId) != null) {
                targetPermanentId = targetPermanentId != null ? targetPermanentId : targetId;
            } else if (targetCardId == null) {
                targetCardId = targetedSpellCardId(gameData, spell, targetId);
            }
        }
        cues.emit(gameData, VisualCueType.SPELL_CAST, VisualCueService.Source.of(spell),
                targetPermanentId, targetPlayerId, targetCardId, null, GameEventAudience.allPlayers());
    }

    private static UUID targetedSpellCardId(GameData gameData, StackEntry spell, UUID targetId) {
        for (StackEntry entry : gameData.stack) {
            if (entry != spell && entry.getCard() != null && entry.getTargetableId().equals(targetId)
                    && entry.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                    && entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY) {
                return entry.getCard().getId();
            }
        }
        return null;
    }

    public void checkSpellCastTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                       Zone castZone, UUID exiledSourcePermanentId,
                                       boolean castFaceDown) {
        emitSpellCastCue(gameData, spellCard);
        gameData.initializeCardIntensity(spellCard);
        for (StackEntry spell : gameData.stack) {
            if (spell.getCard() != null && spell.getCard().getId().equals(spellCard.getId()) && !spell.isCopy()) {
                spell.setAdditionalEnterCounters(gameData.spellAdditionalEnterCounters
                        .getOrDefault(spellCard.getId(), 0));
                // The mana spent to cast it, and the part of it creatures produced, as the spell becomes cast (CR
                // 601.2i): the game's records are cleared once its cast triggers are collected, so the cast triggers
                // read them from the spell's entry (or their snapshot of it) as they resolve.
                spell.setManaSpentToCast(gameData.getSpellCastManaSpent(spellCard.getId()));
                spell.setCreatureManaSpentToCast(gameData.getSpellCastCreatureManaSpent(spellCard.getId()));
                spell.setManaSpentByColorToCast(gameData.spellCastManaSpentByColor.get(spellCard.getId()));
            }
        }
        gameData.recordSpellCastFromZone(castingPlayerId, castZone, spellCard);
        if (castZone == Zone.EXILE && !spellCard.isToken()) {
            gameData.recordCardPlayedFromExile(castingPlayerId);
        }
        if (castZone == Zone.HAND) {
            gameData.recordSpellCastFromHand(spellCard);
        }
        collectCreatureSpellBoonTriggers(gameData, spellCard, castingPlayerId, castFaceDown);
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) {
            for (Permanent source : gameData.playerBattlefields.getOrDefault(castingPlayerId, List.of())) {
                int prowessInstances = gameQueryService.triggerKeywordInstances(gameData, source, Keyword.PROWESS);
                // Each instance of prowess triggers on its own (CR 702.108a); the door adds the times it triggers
                // additionally.
                for (int instance = 0; instance < prowessInstances; instance++) {
                    Permanent snapshot = new Permanent(source);
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(PROWESS_ABILITY,
                            source.getCard(), source, castingPlayerId,
                            source.getCard().getName() + "'s prowess ability",
                            ConditionContext.forPermanent(source, castingPlayerId),
                            entry -> entry.setSourcePermanentSnapshot(snapshot)));
                }
            }
        }
        if (castZone != Zone.HAND) {
            gameData.recordSpellCastFromOutsideHand(castingPlayerId);
            gameData.playersWhoPlayedOrCastFromOutsideHandThisTurn.add(castingPlayerId);
        }

        // "The first spell you cast from anywhere other than your hand each turn has demonstrate" (The Twelfth Doctor):
        // demonstrate is the spell's triggered ability (CR 702.144a), whose "copy it" reads the spell it records.
        if (castZone != Zone.HAND
                && gameData.getSpellsCastFromOutsideHandThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance is a triggered ability of its own, one stack object each (CR 603.3).
                    for (CardEffect grantEffect : grantEffects) {
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setNonTargeting(true);
                        });
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                }
            }
        }

        if (castZone == Zone.EXILE) {
            gameData.expireFloatingEffects(fe ->
                    fe.duration() == EffectDuration.UNTIL_SOURCE_CARD_CAST_FROM_EXILE
                            && fe.effect() instanceof GrantActivatedAbilityEffect grant
                            && spellCard.getId().equals(grant.expirationCardId()));
        }

        gameData.expireFloatingEffects(fe ->
                (fe.duration() == EffectDuration.UNTIL_MATCHING_SPELL_CAST
                        || fe.duration() == EffectDuration.UNTIL_TARGET_PLAYER_MATCHING_SPELL_CAST)
                        && fe.effect() instanceof NextMatchingSpellCostEffect next
                        && next.appliesToPlayer(castingPlayerId, fe.controllerId())
                        && next.appliesToFaceDownCast(castFaceDown)
                        && predicateEvaluationService.matchesCardPredicate(
                                spellCard, next.predicate(), null, gameData, castingPlayerId));

        if (spellCard.hasType(CardType.CREATURE)) {
            gameData.expireFloatingEffectsOnCreatureSpellCast();
        }

        // Opening hand reveal delayed triggers (Chancellor cycle)
        if (!gameData.openingHandRevealTriggers.isEmpty()
                && !gameData.playersWhoCastFirstSpellInGame.contains(castingPlayerId)) {
            gameData.playersWhoCastFirstSpellInGame.add(castingPlayerId);
            StackEntry firstSpell = findStackEntryByCardId(gameData, spellCard.getId());
            for (OpeningHandRevealTrigger trigger : gameData.openingHandRevealTriggers) {
                if (trigger.revealingPlayerId().equals(castingPlayerId)) continue;
                // A delayed triggered ability of the revealed card (CR 603.7): "counter that spell unless that player
                // pays" acts on the spell, its triggering spell, without targeting it.
                StackEntry entry = TriggeredAbilityStackService.madeAbility(trigger.sourceCard(),
                        trigger.revealingPlayerId(), trigger.sourceCard().getName() + "'s ability",
                        List.of(trigger.effect()), spellCard.getId(), Zone.STACK);
                entry.setTriggeringCardId(spellCard.getId());
                entry.setTriggeringSpell(firstSpell);
                entry.setTriggeringPlayerId(castingPlayerId);
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
            }
        }

        // The spell became cast (CR 601.2i): the abilities that trigger on it are the rules event's.
        SpellEvent.Cast castEvent = new SpellEvent.Cast(spellCard, castingPlayerId, castZone, exiledSourcePermanentId);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(castEvent));

        processDelayedControllerSpellCastTriggers(gameData, spellCard, castingPlayerId);

        // Increment keyword (CR keyword): "Whenever you cast a spell, if the amount of mana you spent
        // is greater than this creature's power or toughness, put a +1/+1 counter on it." Driven by the
        // Scryfall-loaded keyword (like Undying) rather than a per-card effect. Read before mana-spent
        // is cleared below.
        collectIncrementTriggers(gameData, spellCard, castingPlayerId);

        int pathOfAncestryScries = gameData.getSpellCastPathOfAncestryManaSpent(spellCard.getId());
        gameData.clearSpellCastPathOfAncestryManaSpent(spellCard.getId());
        for (int i = 0; i < pathOfAncestryScries; i++) {
            StackEntry entry = TriggeredAbilityStackService.madeAbility(spellCard,
                    castingPlayerId,
                    "Path of Ancestry's ability",
                    new ArrayList<>(List.of(new ScryEffect(1)))
            );
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        if (pathOfAncestryScries > 0) {
            gameLogService.append(gameData, GameLog.text("Path of Ancestry triggers."));
        }

        boolean castUsingTreasureMana = gameData.spellCastUsedTreasureMana(spellCard.getId());
        gameData.clearSpellCastManaSpent(spellCard.getId());
        gameData.clearSpellCastManaSources(spellCard.getId());
        gameData.clearSpellCastTreasureMana(spellCard.getId());
        gameData.clearSpellCastCreatureManaSpent(spellCard.getId());

        // Abaddon the Despoiler: during your turn, hand-cast spells with mana value at most the
        // total life lost by opponents this turn have cascade. The spell's effective mana value is
        // snapshotted so X spells use their announced value when the cascade trigger resolves.
        int opponentsLifeLostThisTurn = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(castingPlayerId))
                .mapToInt(playerId -> gameData.lifeLostThisTurn.getOrDefault(playerId, 0))
                .sum();
        if (castZone == Zone.HAND
                && castingPlayerId.equals(gameData.activePlayerId)
                && spellManaValue(gameData, spellCard) <= opponentsLifeLostThisTurn) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        int manaValue = spellManaValue(gameData, spellCard);
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setEventValue(manaValue);
                        });
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to {} for {} (opponents lost {} life)",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId,
                            opponentsLifeLostThisTurn);
                }
            }
        }

        // Sloppity Bilepiper: the next creature spell this turn has cascade. Each granted instance of
        // cascade is its own triggered ability (CR 702.85c), so each goes on the stack separately. The
        // spell's effective mana value is snapshotted so the existing CascadeEffect handler uses it.
        int pendingCreatureSpellCascades = gameData.consumeNextCreatureSpellCascade(
                castingPlayerId, spellCard);
        for (int i = 0; i < pendingCreatureSpellCascades; i++) {
            StackEntry cascadeTrigger = TriggeredAbilityStackService.madeAbility(spellCard,
                    castingPlayerId,
                    spellCard.getName() + "'s ability",
                    new ArrayList<>(List.of(new CascadeEffect())));
            cascadeTrigger.setTriggeringCardId(spellCard.getId());
            cascadeTrigger.setEventValue(spellManaValue(gameData, spellCard));
            triggeredAbilityStackService.putDelayed(gameData, cascadeTrigger);
            gameLogService.append(gameData, GameLog.cardThen(
                    spellCard, " has cascade from Sloppity Bilepiper."));
        }
        if (pendingCreatureSpellCascades > 0) {
            log.info("Game {} - queued {} delayed cascade grant(s) for {}",
                    gameData.id, pendingCreatureSpellCascades, spellCard.getName());
        }

        // "When you next cast a creature spell this turn, ..." delayed triggers (Yuna, Grand Summoner):
        // each goes on the stack above the spell as its own triggered ability.
        for (DelayedSpellCastTrigger delayed : gameData.consumeNextCreatureSpellDelayedTriggers(
                castingPlayerId, spellCard)) {
            StackEntry delayedTrigger = TriggeredAbilityStackService.madeAbility(delayed.source(),
                    castingPlayerId,
                    delayed.source().getName() + "'s ability",
                    new ArrayList<>(List.of(delayed.effect())));
            delayedTrigger.setTriggeringCardId(spellCard.getId());
            triggeredAbilityStackService.putDelayed(gameData, delayedTrigger);
            gameLogService.append(gameData, GameLog.abilityTriggers(delayed.source()));
            log.info("Game {} - delayed cast trigger of {} queued for {}",
                    gameData.id, delayed.source().getName(), spellCard.getName());
        }

        ManaPool spellPaymentPool = gameData.playerManaPools.get(castingPlayerId);
        if (spellPaymentPool != null) {
            boolean instantOrSorcery = spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY);
            boolean red = spellCard.getColors() != null && spellCard.getColors().contains(CardColor.RED);
            int wellspringCopies = spellPaymentPool.takeSpentCopyGrantingMana(ManaCopyRider.INSTANT_OR_SORCERY_SPELL);
            int gogglesCopies = spellPaymentPool.takeSpentCopyGrantingMana(ManaCopyRider.RED_INSTANT_OR_SORCERY_SPELL);
            if (instantOrSorcery) {
                queueManaLinkedSpellCopyTriggers(gameData, spellCard, castingPlayerId, wellspringCopies);
            }
            if (instantOrSorcery && red) {
                queueManaLinkedSpellCopyTriggers(gameData, spellCard, castingPlayerId, gogglesCopies);
            }
        }

        // "When you next cast an instant or sorcery spell this turn, copy that spell" (Chandra, the Firebrand's −2,
        // Teach by Example, Sea Gate Stormcaller): each pending delayed ability that applies to the spell triggers
        // and is used up, one stack object each (CR 603.7), with the object that made it as its source (CR 603.7e).
        List<GameData.PendingSpellCopy> pendingCopies = gameData.pendingNextInstantSorceryCopies.get(castingPlayerId);
        if (pendingCopies != null && !pendingCopies.isEmpty()
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                int manaValue = spellCard.getManaValue();
                if (spellCard.getParsedManaCost() != null) {
                    manaValue += spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount();
                }
                List<GameData.PendingSpellCopy> triggered = new ArrayList<>();
                synchronized (pendingCopies) {
                    for (GameData.PendingSpellCopy pending : List.copyOf(pendingCopies)) {
                        int limitsMet = 0;
                        for (int limit : pending.maxManaValues()) {
                            if (manaValue <= limit) {
                                limitsMet++;
                            }
                        }
                        if (!pending.maxManaValues().isEmpty() && limitsMet == 0) {
                            continue;
                        }
                        pendingCopies.remove(pending);
                        triggered.add(pending.maxManaValues().isEmpty() ? pending
                                : new GameData.PendingSpellCopy(pending.sourceCard(), pending.sourcePermanentId(),
                                limitsMet, null, List.of(), pending.resolution(), pending.link()));
                    }
                }
                StackEntry snapshot = new StackEntry(spellEntry);
                for (GameData.PendingSpellCopy copy : triggered) {
                    CopyControllerCastSpellEffect copyEffect = copy.dynamicCopies() != null
                            // How many copies it makes is counted as it resolves.
                            ? new CopyControllerCastSpellEffect(new StackEntry(spellEntry), castingPlayerId,
                            copy.dynamicCopies())
                            : new CopyControllerCastSpellEffect(snapshot, castingPlayerId, copy.copies());
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(
                            copy.sourceCard() != null ? copy.sourceCard() : spellCard, castingPlayerId,
                            "Copy " + spellCard.getName(), List.of(copyEffect), 0, copy.sourcePermanentId()));
                    if (copy.link() != null) {
                        removeLinkedLoyaltyCopies(gameData, castingPlayerId, copy.link());
                    }
                }
                if (!triggered.isEmpty()) {
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} delayed spell-copy trigger(s) queued for {}",
                            gameData.id, triggered.size(), spellCard.getName());
                }
            }
        }

        // "The next instant or sorcery spell you cast this turn has Storm" (Storm, Force of Nature).
        Integer pendingStorms = gameData.pendingNextInstantSorceryStormThisTurnCount.get(castingPlayerId);
        if (pendingStorms != null && pendingStorms > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int i = 0; i < pendingStorms; i++) {
                    queueGrantedStorm(gameData, spellCard, castingPlayerId, spellEntry,
                            CopyTriggeringSpellEffect.storm());
                }
                gameData.pendingNextInstantSorceryStormThisTurnCount.remove(castingPlayerId);
                log.info("Game {} - {} delayed Storm grant(s) applied to {}",
                        gameData.id, pendingStorms, spellCard.getName());
            }
        }

        Integer pendingSpellCopies = gameData.pendingNextSpellCopyThisTurnCount.get(castingPlayerId);
        if (pendingSpellCopies != null && pendingSpellCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                    default -> false;
                };
                StackEntry snapshot = new StackEntry(spellEntry);
                // Each "copy the next spell you cast this turn" is a delayed triggered ability of the resolution
                // that created it, so each is a stack object of its own (CR 603.7a).
                for (int i = 0; i < pendingSpellCopies; i++) {
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                    snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true)))
                    ));
                }
                gameData.pendingNextSpellCopyThisTurnCount.remove(castingPlayerId);
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} all-spell copy trigger(s) queued for {}",
                        gameData.id, pendingSpellCopies, spellCard.getName());
            }
        }

        List<CopyNextSpellCastThisTurnEffect> pendingFilteredCopies =
                gameData.pendingNextFilteredSpellCopiesThisTurn.get(castingPlayerId);
        if (pendingFilteredCopies != null && !pendingFilteredCopies.isEmpty()) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                Card spellOnStack = SpellTriggers.spellOnStackWithChosenX(
                        spellCard, spellEntry.getXValue());
                List<CopyNextSpellCastThisTurnEffect> matchingCopies = new ArrayList<>();
                List<CopyNextSpellCastThisTurnEffect> remainingCopies = new ArrayList<>();
                for (CopyNextSpellCastThisTurnEffect copyEffect : pendingFilteredCopies) {
                    // "{X} in its mana cost" is read off the printed cost, which the chosen X is substituted into
                    // on the stack.
                    Card filterSubject = copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate
                            ? spellCard : spellOnStack;
                    if (copyEffect.spellFilter() == null
                            || predicateEvaluationService.matchesCardPredicate(
                            filterSubject, copyEffect.spellFilter(), null, gameData, castingPlayerId)) {
                        matchingCopies.add(copyEffect);
                    } else {
                        remainingCopies.add(copyEffect);
                    }
                }
                if (!matchingCopies.isEmpty()) {
                    boolean permanentSpell = switch (spellEntry.getEntryType()) {
                        case CREATURE_SPELL, ENCHANTMENT_SPELL, ARTIFACT_SPELL, PLANESWALKER_SPELL, BATTLE_SPELL -> true;
                        default -> false;
                    };
                    StackEntry snapshot = new StackEntry(spellEntry);
                    // Each "when you next cast a ... spell this turn, copy it" is a delayed triggered ability of
                    // the resolution that created it, so each is a stack object of its own (CR 603.7a).
                    for (CopyNextSpellCastThisTurnEffect copyEffect : matchingCopies) {
                        triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
                                castingPlayerId,
                                "Copy " + spellCard.getName(),
                                new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                        snapshot, castingPlayerId, Set.of(), Set.of(),
                                        copyEffect.removedSupertypes(), permanentSpell, true)))
                        ));
                    }
                    int linkedXCopies = (int) matchingCopies.stream()
                            .filter(copyEffect -> copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate)
                            .count();
                    if (linkedXCopies > 0) {
                        consumeLinkedXAbilityCopies(gameData, castingPlayerId, linkedXCopies);
                    }
                    if (remainingCopies.isEmpty()) {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.remove(castingPlayerId);
                    } else {
                        gameData.pendingNextFilteredSpellCopiesThisTurn.put(castingPlayerId, remainingCopies);
                    }
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                    log.info("Game {} - {} filtered spell-copy trigger(s) queued for {}",
                            gameData.id, matchingCopies.size(), spellCard.getName());
                }
            }
        }

        // "Until end of turn, whenever you cast an instant or sorcery spell, copy it"
        // (e.g. The Mirari Conjecture chapter III). Each granting ability triggers on its own.
        int spellCopyGrants = gameData.spellCopyGrantsUntilEndOfTurn.getOrDefault(castingPlayerId, 0);
        if (spellCopyGrants > 0
                && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            // Find the spell on the stack to create a snapshot
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int grant = 0; grant < spellCopyGrants; grant++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(copyEffect))
                    ));
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (The Mirari Conjecture)."));
                    log.info("Game {} - {} spell-copy trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        // Conspire (CR 702.78): "When you [tap the two creatures], copy it and you may choose a new
        // target for the copy." Each conspire instance paid during casting queues its own copy
        // trigger; the count is recorded in gameData.conspiredSpellCopyCounts.
        Integer conspireCopies = gameData.conspiredSpellCopyCounts.remove(spellCard.getId());
        if (conspireCopies != null && conspireCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int i = 0; i < conspireCopies; i++) {
                    StackEntry snapshot = new StackEntry(spellEntry);
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(snapshot, castingPlayerId);
                    // Conspire's "when you cast this spell, if its conspire cost was paid, copy it" is the spell's
                    // triggered ability (CR 702.78a).
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            new TriggeredAbility(Triggers.youCastThisSpell(), copyEffect), spellCard, null,
                            castingPlayerId, "Copy " + spellCard.getName() + " (Conspire)",
                            ConditionContext.forCard(spellCard, castingPlayerId), NO_EXTRA_RECORD));
                    gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Conspire)."));
                    log.info("Game {} - {} conspire copy trigger queued for {}",
                            gameData.id, spellCard.getName(), castingPlayerId);
                }
            }
        }

        Integer casualtyCopies = gameData.casualtySpellCopyCounts.remove(spellCard.getId());
        if (casualtyCopies != null && casualtyCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                for (int copyNumber = 0; copyNumber < casualtyCopies; copyNumber++) {
                    CopyControllerCastSpellEffect copyEffect =
                            new CopyControllerCastSpellEffect(new StackEntry(spellEntry), castingPlayerId);
                    // Casualty's "when you cast this spell, if a casualty cost was paid for it, copy it" is the
                    // spell's triggered ability (CR 702.153a).
                    triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                            new TriggeredAbility(Triggers.youCastThisSpell(), copyEffect), spellCard, null,
                            castingPlayerId, "Copy " + spellCard.getName() + " (Casualty)",
                            ConditionContext.forCard(spellCard, castingPlayerId), NO_EXTRA_RECORD));
                }
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied (Casualty)."));
                log.info("Game {} - {} casualty copy trigger(s) queued for {}",
                        gameData.id, casualtyCopies, spellCard.getName());
            }
        }

        // "When you cast this spell, ..." abilities of the just-cast card itself (it's a spell on the stack, not a
        // permanent), each one object on the stack (collectSelfCastAbilities): replicate, demonstrate, the card's own
        // abilities (their options judged as they trigger) and the ones perpetually granted to it.
        List<List<CardEffect>> selfCastAbilities = new ArrayList<>();
        if (spellCard.getManaCost() != null) {
            // A granted replicate (CR 702.56a): "When you cast this spell, if a replicate cost was paid for it, copy it
            // for each time its replicate cost was paid", its intervening "if" judged here and again as it resolves.
            // The spell's own replicate with the same cost counts the same payments, so it triggers alone.
            StackEntry replicatedSpell = findStackEntryByCardId(gameData, spellCard.getId());
            gameQueryService.getSpellCastingAbilityGrantValues(
                            gameData, castingPlayerId, spellCard, Keyword.REPLICATE, castZone).stream()
                    .map(value -> value > 0 ? "{" + value + "}" : spellCard.getManaCost())
                    .distinct()
                    .filter(replicateCost -> spellCard.getTriggerEffects(Triggers.youCastThisSpell(), Zone.STACK).stream()
                            .noneMatch(effect -> countsReplicatePayments(effect, replicateCost)))
                    .filter(replicateCost -> replicatedSpell != null
                            && replicatedSpell.getRepeatedAdditionalCosts().contains(replicateCost))
                    .map(replicateCost -> new ConditionalEffect(new RepeatedAdditionalCostPaid(replicateCost),
                            new CopyTriggeringSpellEffect().withCopies(new RepeatedAdditionalCostCount(replicateCost))))
                    .forEach(replicate -> selfCastAbilities.add(List.of(replicate)));
        }

        ManaPool castingPlayerPool = gameData.playerManaPools.get(castingPlayerId);
        int pendingSpellOrAbilityCopies = castingPlayerPool == null ? 0
                : castingPlayerPool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pendingSpellOrAbilityCopies > 0) {
            StackEntry spellEntry = null;
            for (StackEntry se : gameData.stack) {
                if (se.getTargetableId().equals(spellCard.getId())) {
                    spellEntry = se;
                    break;
                }
            }
            if (spellEntry != null) {
                StackEntry snapshot = new StackEntry(spellEntry);
                boolean permanentSpell = switch (spellEntry.getEntryType()) {
                    case CREATURE_SPELL, ARTIFACT_SPELL, ENCHANTMENT_SPELL, PLANESWALKER_SPELL,
                            BATTLE_SPELL -> true;
                    default -> false;
                };
                // Each mana with "when you spend this mana to cast a spell, copy that spell" is a delayed triggered
                // ability of the mana ability that added it, so each is a stack object of its own (CR 603.7a).
                for (int i = 0; i < pendingSpellOrAbilityCopies; i++) {
                    triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
                            castingPlayerId,
                            "Copy " + spellCard.getName(),
                            new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                                    snapshot, castingPlayerId, Set.of(), Set.of(), permanentSpell, true)))
                    ));
                }
                gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
                log.info("Game {} - {} mana-linked spell-or-ability copy trigger(s) queued for {}",
                        gameData.id, pendingSpellOrAbilityCopies, spellCard.getName());
            }
        }
        // Each granted instance of demonstrate triggers separately (e.g. two Silverquill Lecturers).
        gameQueryService.getSpellCastingAbilityGrantValues(
                        gameData, castingPlayerId, spellCard, Keyword.DEMONSTRATE, castZone)
                .forEach(grant -> selfCastAbilities.add(List.of(new MayEffect(
                        new DemonstrateEffect(), "Copy " + spellCard.getName() + "?"))));
        StackEntry selfCastSpellEntry = gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(spellCard.getId()))
                .findFirst()
                .orElse(null);
        ConditionContext selfCastContext = selfCastSpellEntry != null
                ? ConditionContext.forStackEntry(selfCastSpellEntry)
                : ConditionContext.forCard(spellCard, castingPlayerId);
        collectSelfCastAbilities(gameData, spellCard, castingPlayerId, castZone, selfCastSpellEntry, selfCastContext,
                selfCastAbilities);

        if (castZone == Zone.HAND && (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY))) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        int manaValue = spellManaValue(gameData, spellCard);
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setEventValue(manaValue);
                        });
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to {} cast from hand for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }
        // COMMAND_ZONE_ON_CONTROLLER_CASTS_SPELL — Eminence and similar command-zone spell-cast triggers

        if (castZone == Zone.HAND && spellCard.hasType(CardType.ENCHANTMENT)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    GameQueryService.StaticBonus staticBonus = gameQueryService.computeStaticBonus(gameData, perm);
                    if (staticBonus.losesAllAbilities() || staticBonus.losesAllNonManaAbilities()
                            || gameQueryService.hasLostPrintedAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_ENCHANTMENT_FROM_HAND);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        int manaValue = spellManaValue(gameData, spellCard);
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect, entry -> {
                            entry.setTriggeringCardId(spellCard.getId());
                            entry.setEventValue(manaValue);
                        });
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to enchantment {} cast from hand for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        // "The first spell you cast each turn has cascade" (Maelstrom Nexus). A permanent-granted
        // keyword: a static GrantCastTriggerToSpellsEffect on the caster's battlefield. recordSpellCast runs before this method in
        // every cast path, so a count of 1 identifies the caster's first spell of the turn. The held
        // CascadeEffect is queued keyed to the just-cast spell so CascadeEffectHandler's threshold is
        // the spell's mana value (not the granting permanent's). One trigger per granting permanent.
        if (gameQueryService.cardHasSubtype(spellCard, CardSubtype.SLIVER, gameData, castingPlayerId)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_SLIVER_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        int xValue = grantedCascadeSpell == null ? 0 : grantedCascadeSpell.getXValue();
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect,
                                entry -> entry.setXValue(xValue));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to Sliver spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (gameData.getSpellsCastThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        int xValue = grantedCascadeSpell == null ? 0 : grantedCascadeSpell.getXValue();
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect,
                                entry -> entry.setXValue(xValue));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        // "The first spell you cast each turn that mana from a Treasure was spent to cast has
        // cascade" (Rain of Riches). Unlike the ordinary first-spell marker above, a spell cast
        // without Treasure mana does not consume this grant.
        if (castUsingTreasureMana
                && gameData.getSpellsCastUsingTreasureManaThisTurnCount(castingPlayerId) == 1) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA);
                    if (grantEffects.isEmpty()) continue;

                    // Each granted instance triggers separately (CR 702.85c).
                    for (CardEffect grantEffect : grantEffects) {
                        StackEntry grantedCascadeSpell = findStackEntryByCardId(gameData, spellCard.getId());
                        int xValue = grantedCascadeSpell == null ? 0 : grantedCascadeSpell.getXValue();
                        putSpellKeywordAbility(gameData, spellCard, castingPlayerId, grantEffect,
                                entry -> entry.setXValue(xValue));
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants cascade to first Treasure-mana-funded spell {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        if (spellCard.hasType(CardType.INSTANT) || spellCard.hasType(CardType.SORCERY)) {
            List<Permanent> casterBattlefield = gameData.playerBattlefields.get(castingPlayerId);
            if (casterBattlefield != null) {
                for (Permanent perm : new ArrayList<>(casterBattlefield)) {
                    if (perm.isLosesAllAbilitiesUntilEndOfTurn()
                            || gameQueryService.hasLostAllAbilities(gameData, perm)) continue;
                    List<CardEffect> grantEffects = castTriggerGrants(perm, SpellCastTriggerGrant.STORM_TO_INSTANT_OR_SORCERY);
                    if (grantEffects.isEmpty()) continue;

                    StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
                    if (spellEntry == null) continue;

                    for (CardEffect grantedStorm : grantEffects) {
                        queueGrantedStorm(gameData, spellCard, castingPlayerId, spellEntry, grantedStorm);
                    }
                    gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                    log.info("Game {} - {} grants Storm to {} for {}",
                            gameData.id, perm.getCard().getName(), spellCard.getName(), castingPlayerId);
                }
            }
        }

        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * The triggered abilities a permanent's static {@link GrantCastTriggerToSpellsEffect}s of the given kind give
     * the spell being cast, in the order the permanent lists them.
     */
    private static List<CardEffect> castTriggerGrants(Permanent perm, SpellCastTriggerGrant grant) {
        return perm.getCard().getStaticEffects().stream()
                .filter(GrantCastTriggerToSpellsEffect.class::isInstance)
                .map(GrantCastTriggerToSpellsEffect.class::cast)
                .filter(granting -> granting.grant() == grant)
                .map(GrantCastTriggerToSpellsEffect::ability)
                .toList();
    }

    /**
     * Hands the storm an effect gave {@code spellCard}, the spell just cast ({@code spellEntry}), to the stack door:
     * storm is the spell's triggered ability (CR 702.40a), the granted {@code effect} as written, with that spell as its
     * triggering spell and the spell's places in the turn's cast order, from which its copy count is read as it
     * resolves.
     */
    private void queueGrantedStorm(GameData gameData, Card spellCard, UUID controllerId, StackEntry spellEntry,
                                   CardEffect effect) {
        putSpellKeywordAbility(gameData, spellCard, controllerId, effect,
                entry -> SpellTriggers.recordTriggeringSpell(gameData, entry, spellEntry));
        log.info("Game {} - granted Storm trigger queued for {}", gameData.id, spellEntry.getCard().getName());
    }

    /**
     * Hands {@code effect}, a keyword triggered ability of {@code spellCard} that a static ability or an effect gave the
     * spell just cast ("when you cast this spell": cascade, storm, demonstrate), to the stack door as the spell's own
     * ability (CR 702.85a, 702.40a, 702.144a), with no source permanent, stamped with {@code record}.
     */
    private void putSpellKeywordAbility(GameData gameData, Card spellCard, UUID controllerId, CardEffect effect,
                                        TriggerRecord record) {
        triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(
                new TriggeredAbility(Triggers.youCastThisSpell(), effect), spellCard, null, controllerId,
                spellCard.getName() + "'s ability", ConditionContext.forCard(spellCard, controllerId), record));
    }

    private int spellManaValue(GameData gameData, Card spellCard) {
        StackEntry spellEntry = findStackEntryByCardId(gameData, spellCard.getId());
        if (spellEntry != null && spellEntry.isCastFaceDown()) {
            return 0;
        }
        return spellCard.getManaValue() + (spellEntry == null || spellCard.getParsedManaCost() == null
                ? 0 : spellEntry.getXValue() * spellCard.getParsedManaCost().getXSymbolCount());
    }

    /**
     * Hands {@code spellCard}'s keyword "when you cast this spell" abilities that a grant gave it to the stack door, each
     * one object (CR 603.3), {@code abilities} the effects of each: a granted replicate (CR 702.56a, its intervening "if"
     * wrapped around its copy) and demonstrate (CR 702.144a). Each is the spell's own ability, recorded as
     * {@link SpellTriggers#putSelfCastAbility} records one; a "you may" is asked as it resolves (CR 603.5).
     */
    private void collectSelfCastAbilities(GameData gameData, Card spellCard, UUID castingPlayerId, Zone castZone,
                                          StackEntry spellEntry, ConditionContext context,
                                          List<List<CardEffect>> abilities) {
        for (List<CardEffect> effects : abilities) {
            if (effects.isEmpty()) {
                continue;
            }
            spellTriggers.putSelfCastAbility(gameData, spellCard, castingPlayerId, castZone, spellEntry,
                    new TriggeredAbility(Triggers.youCastThisSpell(), effects.toArray(CardEffect[]::new)), context);
        }
    }

    /** Whether {@code effect} is a replicate copy counting the payments of {@code replicateCost}. */
    private static boolean countsReplicatePayments(CardEffect effect, String replicateCost) {
        return switch (effect) {
            case CopyTriggeringSpellEffect copy -> copy.dynamicCopies() instanceof RepeatedAdditionalCostCount count
                    && replicateCost.equals(count.manaCost());
            case ConditionalEffect conditional -> countsReplicatePayments(conditional.wrapped(), replicateCost);
            default -> false;
        };
    }

    /**
     * Records that {@code sacrificingPlayerId} sacrificed {@code sacrificedCard}, for "this turn" counts and sacrifice
     * boons. The abilities that trigger on the sacrifice ("whenever you sacrifice a permanent") are the trigger
     * pipeline's: the departure's rules event carries the sacrifice.
     */
    public void recordSacrifice(GameData gameData, UUID sacrificingPlayerId, Card sacrificedCard) {
        gameData.playersWhoSacrificedPermanentsThisTurn.add(sacrificingPlayerId);
        gameData.recordSacrificedPermanent(sacrificingPlayerId, sacrificedCard);
        checkSacrificeBoonTriggers(gameData, sacrificingPlayerId);
        if (gameData.simultaneousDyingPermanents.isEmpty()) {
            playerInputService.processNextMayAbility(gameData);
        }
    }

    /** Fires finite digital sacrifice boons once for each successful permanent sacrifice. */
    private void checkSacrificeBoonTriggers(GameData gameData, UUID sacrificingPlayerId) {
        if (sacrificingPlayerId == null) {
            return;
        }
        if (gameData.simultaneousSacrificeDepth > 0
                && !gameData.sacrificeBoonBatchPlayerIds.add(sacrificingPlayerId)) {
            return;
        }
        List<SacrificeBoonWatcher> watchers = List.copyOf(gameData.sacrificeBoonWatchers);
        for (SacrificeBoonWatcher watcher : watchers) {
            if (!watcher.controllerId().equals(sacrificingPlayerId)
                    || watcher.remainingUses() <= 0
                    || !gameData.sacrificeBoonWatchers.remove(watcher)) {
                continue;
            }

            int remainingUses = watcher.remainingUses() - 1;
            if (remainingUses > 0) {
                gameData.sacrificeBoonWatchers.add(new SacrificeBoonWatcher(
                        watcher.controllerId(), watcher.sourceCard(), watcher.effect(), remainingUses));
            }

            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect()))));
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} sacrifice boon triggers ({} use(s) remain)", gameData.id,
                    watcher.sourceCard().getName(), remainingUses);
        }
    }

    private StackEntry findStackEntryByCardId(GameData gameData, UUID cardId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getCard() != null && entry.getCard().getId().equals(cardId))
                .findFirst()
                .orElse(null);
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

    /**
     * A single "when you next cast a spell with {X} in its mana cost or activate an ability with {X} in
     * its activation cost" delayed trigger (Magus Lucea Kane) is registered as one spell copy and one
     * ability copy; when the ability half fires, the spell half is used up with it.
     */
    private void consumeLinkedXSpellCopies(GameData gameData, UUID playerId, int count) {
        List<CopyNextSpellCastThisTurnEffect> pending = gameData.pendingNextFilteredSpellCopiesThisTurn.get(playerId);
        if (pending == null) {
            return;
        }
        List<CopyNextSpellCastThisTurnEffect> remaining = new ArrayList<>(pending);
        for (int i = 0; i < count; i++) {
            CopyNextSpellCastThisTurnEffect linked = remaining.stream()
                    .filter(copyEffect -> copyEffect.spellFilter() instanceof CardHasXInManaCostPredicate)
                    .findFirst().orElse(null);
            if (linked == null) {
                break;
            }
            remaining.remove(linked);
        }
        if (remaining.isEmpty()) {
            gameData.pendingNextFilteredSpellCopiesThisTurn.remove(playerId);
        } else {
            gameData.pendingNextFilteredSpellCopiesThisTurn.put(playerId, remaining);
        }
    }

    /** The spell half of a linked X delayed trigger used up its ability half; see {@link #consumeLinkedXSpellCopies}. */
    private void consumeLinkedXAbilityCopies(GameData gameData, UUID playerId, int count) {
        gameData.pendingNextXActivatedAbilityCopyThisTurnCount.computeIfPresent(playerId,
                (id, pendingCount) -> pendingCount - count > 0 ? pendingCount - count : null);
    }

    /**
     * One activation of an activated ability, as it is once the ability is on the stack and its costs are paid, which
     * is when "whenever [a player] activates an ability" abilities trigger (CR 602.2b, CR 601.2i).
     *
     * @param activatingPlayerId             the player who activated the ability
     * @param abilityEntry                   the activated ability on the stack, with its targets and X; for a mana
     *                                       ability, which doesn't use the stack, the entry it would have been
     * @param ability                        the activated ability
     * @param activatedPermanent             the permanent whose ability it is, as it last existed when a cost removed
     *                                       it, or {@code null} for an ability of a card that isn't a permanent (in a
     *                                       hand, a graveyard, exile or the command zone; CR 109.2)
     * @param activatedPermanentControllerId that permanent's controller as the activation began; the activating
     *                                       player for an ability of a card in a hand or a graveyard
     * @param manaAbility                    whether it is a mana ability (CR 605.1a)
     */
    public record Activation(UUID activatingPlayerId, StackEntry abilityEntry, ActivatedAbility ability,
                             Permanent activatedPermanent, UUID activatedPermanentControllerId,
                             boolean manaAbility) {}

    /**
     * An activated ability was activated: it is on the stack and its costs are paid (CR 602.2b, 601.2i). The delayed
     * "when you next activate ..., copy that ability" abilities trigger first ({@link #collectDelayedActivationCopies}),
     * then the activation is the rules event the "whenever [a player] activates an ability" abilities trigger on
     * ({@link SpellEvent.Activated}), with the life paid to activate it; then the abilities that choose their targets as
     * they go on the stack (CR 603.3d) choose them, above the activated ability.
     */
    public void collectActivationTriggers(GameData gameData, Activation activation) {
        if (activation.abilityEntry() == null) {
            return;
        }
        int lifePaid = gameData.activationLifePaid;
        if (!activation.manaAbility()) {
            gameData.trackingActivationLifePaid = false;
            gameData.activationLifePaid = 0;
        }
        collectDelayedActivationCopies(gameData, activation);
        boolean treasureMana = gameData.abilityActivationUsedTreasureMana
                .getOrDefault(activation.abilityEntry().getCard().getId(), false);
        SpellEvent.Activated event = new SpellEvent.Activated(activation.activatingPlayerId(),
                activation.abilityEntry(), activation.ability(), activation.activatedPermanent(),
                activation.activatedPermanentControllerId(), activation.manaAbility(), lifePaid, treasureMana);
        gameData.rulesEvents.separately(() -> gameData.rulesEvents.emit(event));
        // An ability that chooses its targets as it goes on the stack (CR 603.3d) chooses them now, above the
        // activated ability; a mana ability's wait with it.
        if (!activation.manaAbility()) {
            triggeredAbilityQueueService.beginStackDoorChoices(gameData);
        }
    }

    /**
     * The delayed triggered abilities an earlier resolution created that trigger on {@code activation} (CR 603.7): "when
     * you next activate an ability with {X} in its activation cost this turn, copy that ability" (Magus Lucea Kane),
     * and for an ability that isn't a mana ability "when you next activate a loyalty ability" and "... an exhaust
     * ability" and "until end of turn, whenever you activate an ability of a planeswalker of the chosen type" (Leori,
     * Sparktouched Hunter). Each is a stack object of its own (CR 603.7a) that copies the ability as it was activated.
     */
    private void collectDelayedActivationCopies(GameData gameData, Activation activation) {
        UUID activatingPlayerId = activation.activatingPlayerId();
        StackEntry abilityEntry = activation.abilityEntry();
        ActivatedAbility ability = activation.ability();

        Integer pendingXCopies = gameData.pendingNextXActivatedAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingXCopies != null && pendingXCopies > 0
                && ability.getManaCost() != null && new ManaCost(ability.getManaCost()).hasX()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            for (int i = 0; i < pendingXCopies; i++) {
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(abilityEntry.getCard(),
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s ability",
                        new ArrayList<>(List.of(
                                new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId)))
                ));
            }
            gameData.pendingNextXActivatedAbilityCopyThisTurnCount.remove(activatingPlayerId);
            consumeLinkedXSpellCopies(gameData, activatingPlayerId, pendingXCopies);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s ability is copied."));
            log.info("Game {} - {} delayed X-ability copy trigger(s) queued for {}",
                    gameData.id, pendingXCopies, abilityEntry.getCard().getName());
        }
        if (activation.manaAbility()) {
            return;
        }

        Integer pendingLoyaltyCopies = gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingLoyaltyCopies != null && pendingLoyaltyCopies > 0 && ability.getLoyaltyCost() != null) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources =
                    gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingLoyaltyCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                Card copySourceCard = source != null ? source.card() : abilityEntry.getCard();
                UUID copySourcePermanentId = source != null ? source.sourcePermanentId() : null;
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(copySourceCard,
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s loyalty ability",
                        List.of(new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId)),
                        0,
                        copySourcePermanentId
                ));
            }
            gameData.pendingNextLoyaltyAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextLoyaltyAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            // A delayed ability shared with the next instant or sorcery spell is used up (Repeated Reverberation).
            Set<UUID> links = sources.stream().map(GameData.PendingCopySource::link)
                    .filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
            List<GameData.PendingSpellCopy> spellCopies = gameData.pendingNextInstantSorceryCopies.get(activatingPlayerId);
            if (spellCopies != null && !links.isEmpty()) {
                spellCopies.removeIf(copy -> copy.link() != null && links.contains(copy.link()));
            }
        }

        Integer pendingExhaustCopies = gameData.pendingNextExhaustAbilityCopyThisTurnCount.get(activatingPlayerId);
        if (pendingExhaustCopies != null && pendingExhaustCopies > 0 && ability.isExhaustAbility()) {
            StackEntry snapshot = new StackEntry(abilityEntry);
            List<GameData.PendingCopySource> sources = gameData.pendingNextExhaustAbilityCopySourcesThisTurn
                    .getOrDefault(activatingPlayerId, List.of());
            for (int i = 0; i < pendingExhaustCopies; i++) {
                GameData.PendingCopySource source = i < sources.size() ? sources.get(i) : null;
                triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(source != null ? source.card() : abilityEntry.getCard(),
                        activatingPlayerId,
                        "Copy " + abilityEntry.getCard().getName() + "'s exhaust ability",
                        new ArrayList<>(List.of(
                                new CopyControllerActivatedAbilityEffect(snapshot, ability, activatingPlayerId))),
                        0,
                        source != null ? source.sourcePermanentId() : null
                ));
            }
            gameData.pendingNextExhaustAbilityCopyThisTurnCount.remove(activatingPlayerId);
            gameData.pendingNextExhaustAbilityCopySourcesThisTurn.remove(activatingPlayerId);
            gameLogService.append(gameData, GameLog.cardThen(abilityEntry.getCard(), "'s exhaust ability is copied."));
            log.info("Game {} - {} delayed exhaust-ability copy trigger(s) queued for {}",
                    gameData.id, pendingExhaustCopies, abilityEntry.getCard().getName());
        }

        Permanent activatedPermanent = activation.activatedPermanent();
        for (GameData.ChosenSubtypeActivationCopy delayedCopy : gameData.chosenSubtypeActivationCopiesThisTurn) {
            if (!delayedCopy.controllerId().equals(activatingPlayerId) || activatedPermanent == null
                    || !predicateEvaluationService.matchesPermanentPredicate(activatedPermanent,
                    new PermanentAllOfPredicate(List.of(
                            new PermanentIsPlaneswalkerPredicate(),
                            new PermanentHasSubtypePredicate(delayedCopy.subtype()))),
                    FilterContext.of(gameData).withSourceControllerId(activatingPlayerId))) {
                continue;
            }
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(delayedCopy.sourceCard(),
                    activatingPlayerId,
                    delayedCopy.sourceCard().getName() + "'s delayed ability",
                    new ArrayList<>(List.of(new CopyControllerActivatedAbilityEffect(
                            new StackEntry(abilityEntry), ability, activatingPlayerId))),
                    null,
                    delayedCopy.sourcePermanentId()
            ));
            gameLogService.append(gameData, GameLog.abilityTriggers(delayedCopy.sourceCard()));
            log.info("Game {} - {} delayed trigger copies {}'s ability", gameData.id,
                    delayedCopy.sourceCard().getName(), abilityEntry.getCard().getName());
        }
    }

    /**
     * The creature-death triggered abilities that aren't abilities of an object the trigger pipeline indexes: the
     * global ones a resolved spell or ability registered (CR 603.7), emblems' and the delayed "when that creature
     * dies" watchers. They trigger as {@code dyingPermanent}, which
     * {@code dyingControllerId} controlled, dies.
     */
    public void checkCreatureDeathRegisteredTriggers(GameData gameData, UUID dyingControllerId,
                                                     Permanent dyingPermanent, int dyingPowerAtDeath,
                                                     int dyingToughnessAtDeath) {
        Card dyingCard = dyingPermanent.getCard();
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            collectRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId, dyingPermanent,
                    dyingPowerAtDeath, dyingToughnessAtDeath);
        }
        collectEmblemCreatureDeathTriggers(gameData, dyingCard, dyingPermanent, dyingControllerId);
        collectCreatureDeathTriggerWatchers(gameData, dyingPermanent);
    }

    private void collectRegisteredCreatureDeathTrigger(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                       UUID dyingControllerId, Permanent dyingPermanent,
                                                       int dyingPowerAtDeath, int dyingToughnessAtDeath) {
        ZoneChangeTrigger trigger = creatureDeathTrigger(watcher.ability());
        if (trigger == null) {
            return;
        }
        Card dyingCard = dyingPermanent.getCard();
        if (watcher.watchedPermanentId() != null) {
            // "When it dies this turn": a delayed trigger watching one creature (Desperate Measures).
            if (trigger.relation() != ObjectRelation.THIS
                    || !dyingPermanent.getId().equals(watcher.watchedPermanentId())) {
                return;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setTargetId(dyingControllerId);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            entry.setTriggeringPermanentControllerId(dyingControllerId);
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            return;
        }
        if (trigger.relation() == ObjectRelation.THIS) {
            return;
        }
        if (trigger.relation().youControl() && !watcher.controllerId().equals(dyingControllerId)) {
            return;
        }
        // "Until end of turn, whenever a creature dies, its controller ...": the dying creature's controller rides
        // as the entry's non-targeting player, its toughness as the event value; "a nontoken creature you control
        // dies" its controller and power; "a nontoken creature dies" binds the card that died (Consumed by History);
        // "whenever a creature you control dies" names nothing. The other relations and filters are no such ability's.
        switch (trigger.relation()) {
            case HAUNTED, EQUIPPED, ENCHANTED_PLAYER_CONTROLS, DAMAGED_BY_THIS, DAMAGED_BY_ATTACHED,
                 OPPONENT_CONTROLS -> {
                return;
            }
            default -> {
            }
        }
        PermanentPredicate filter = trigger.filter();
        PermanentPredicate narrowed = filter instanceof PermanentAllOfPredicate allOf
                && !filter.equals(Triggers.NONTOKEN_CREATURE) && !allOf.predicates().isEmpty()
                ? allOf.predicates().getFirst() : filter;
        boolean youControl = trigger.relation().youControl();
        if (Triggers.NONTOKEN_CREATURE.equals(narrowed)) {
            if (!dyingCard.isToken()) {
                if (youControl) {
                    enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId,
                            Math.max(0, dyingPowerAtDeath));
                } else {
                    enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingCard.getId(), 0);
                }
            }
        } else if (Triggers.CREATURE_OR_PLANESWALKER.equals(narrowed)
                || Triggers.ARTIFACT_OR_CREATURE.equals(narrowed)) {
            return;
        } else if (youControl) {
            enqueueRegisteredCreatureDeathTrigger(gameData, watcher, null, 0);
        } else {
            enqueueRegisteredCreatureDeathTrigger(gameData, watcher, dyingControllerId,
                    Math.max(0, dyingToughnessAtDeath));
        }
    }

    private void enqueueRegisteredCreatureDeathTrigger(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                       UUID targetId, int eventValue) {
        StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
        entry.setTargetId(targetId);
        entry.setEventValue(eventValue);
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        log.info("Game {} - {} temporary global creature-death trigger fires",
                gameData.id, watcher.sourceCard().getName());
    }

    /** The "whenever a creature dies" trigger condition of {@code ability}, or {@code null} when it has none. */
    private static ZoneChangeTrigger creatureDeathTrigger(TriggeredAbility ability) {
        for (TriggerCondition condition : ability.conditions()) {
            if (condition.trigger() instanceof ZoneChangeTrigger trigger
                    && trigger.from().equals(Set.of(Zone.BATTLEFIELD)) && trigger.to() == Zone.GRAVEYARD
                    && trigger.causes().isEmpty()) {
                return trigger;
            }
        }
        return null;
    }

    /**
     * Records on the triggered abilities already on the stack whose source is an Aura enchanting the permanent that
     * dies the permanent's last-known controller (CR 608.2h) and the Aura as it is now, whatever their effects are:
     * "enchanted permanent's controller" can no longer be read from the permanent once it has left.
     */
    public void recordEnchantedPermanentDeparture(GameData gameData, UUID dyingPermanentId,
                                                  UUID dyingPermanentControllerId) {
        Map<UUID, Permanent> auras = new LinkedHashMap<>();
        gameData.forEachPermanent((playerId, perm) -> auras.put(perm.getId(), perm));
        gameData.simultaneousDyingPermanents.forEach(auras::putIfAbsent);
        for (Permanent perm : auras.values()) {
            if (!dyingPermanentId.equals(perm.getAttachedTo())
                    || perm.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                continue;
            }
            for (StackEntry pending : gameData.stack) {
                if (pending.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && perm.getId().equals(pending.getSourcePermanentId())) {
                    pending.rememberRemovedPermanentController(dyingPermanentId, dyingPermanentControllerId);
                    if (pending.getSourcePermanentSnapshot() == null) {
                        pending.setSourcePermanentSnapshot(new Permanent(perm));
                    }
                }
            }
        }
    }

    /**
     * The emblems' "whenever [a filter permanent] is put into your graveyard from the battlefield" abilities
     * ({@code Triggers.putIntoGraveyardFrom(BATTLEFIELD, filter, YOU_OWN)}; Daretti, Scrap Savant's "return that card to
     * the battlefield at the beginning of the next end step") for {@code departed}, which {@code controllerId}
     * controlled and which was just put into {@code graveyardOwnerId}'s graveyard as {@code card}. Abilities of emblems
     * function in the command zone (CR 114.4); a permanent goes to its owner's graveyard, so "your graveyard" is the
     * emblem controller's own. This is a leaves-the-battlefield ability (CR 603.6c): its filter is judged on the
     * permanent as it last existed on the battlefield (CR 603.10a), and a token triggers it too before it ceases to
     * exist (CR 111.7). Each matching ability goes on the stack once as one object of its effects (CR 603.3), the
     * emblem's source card as its source, the permanent's card ("that card", for this stay in the graveyard,
     * CR 400.7) and last-known permanent as its triggering card and permanent. The reader judges no other relation,
     * cause, option or target; an ability with one fails loudly instead of triggering wrongly. The emblems'
     * "whenever a creature dies" abilities are {@link #collectEmblemCreatureDeathTriggers}'.
     */
    public void checkEmblemPutIntoGraveyardFromBattlefieldTriggers(GameData gameData, UUID graveyardOwnerId,
                                                                   UUID controllerId, Card card,
                                                                   Permanent departed) {
        if (card == null || gameData.emblems.stream().allMatch(emblem -> emblem.triggeredAbilities().isEmpty())) {
            return;
        }
        Permanent lastKnown = departedAsItLastExisted(gameData, departed);
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesPutIntoYourGraveyardFromBattlefield(gameData, emblem, ability, graveyardOwnerId,
                        lastKnown)) {
                    putEmblemGraveyardFromBattlefieldAbilityOnStack(gameData, emblem, ability, card, lastKnown,
                            controllerId);
                }
            }
        }
    }

    /**
     * {@code departed} as the departure's rules event holds it, with the characteristics it left the battlefield
     * with; {@code departed} itself when the log no longer holds that event.
     */
    private static Permanent departedAsItLastExisted(GameData gameData, Permanent departed) {
        List<RulesEventBatch> pending = gameData.rulesEvents.pending();
        for (int index = pending.size() - 1; index >= 0; index--) {
            List<RulesEvent> events = pending.get(index).events();
            for (int eventIndex = events.size() - 1; eventIndex >= 0; eventIndex--) {
                if (events.get(eventIndex) instanceof ZoneChangeEvent zoneChange && zoneChange.leftBattlefield()
                        && zoneChange.permanent() != null
                        && zoneChange.permanent().getId().equals(departed.getId())) {
                    return zoneChange.permanent();
                }
            }
        }
        return departed;
    }

    /**
     * Whether {@code ability} of {@code emblem} triggers on {@code lastKnown} being put into {@code graveyardOwnerId}'s
     * graveyard from the battlefield. A battlefield-to-graveyard trigger whose relation isn't an owner's is the
     * creature-death reader's.
     */
    private boolean emblemSeesPutIntoYourGraveyardFromBattlefield(GameData gameData, Emblem emblem,
                                                                  TriggeredAbility ability, UUID graveyardOwnerId,
                                                                  Permanent lastKnown) {
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger)
                    || !isPutIntoYourGraveyardFromBattlefield(trigger)) {
                continue;
            }
            if (!trigger.causes().isEmpty() || !trigger.excludedTo().isEmpty()
                    || trigger.causedBy() != com.github.laxika.magicalvibes.model.trigger.PlayerRelation.ANY
                    || trigger.duringYourTurn() || trigger.castingSpell() != null
                    || !ability.options().equals(TriggerOptions.DEFAULT) || ability.targetFilter() != null
                    || ability.effects().stream().anyMatch(effect -> effect.effect().targetSpec() != TargetSpec.NONE)) {
                throw new IllegalStateException(emblemName(emblem) + " has a graveyard ability its reader doesn't "
                        + "judge: " + ability);
            }
            if (emblem.controllerId().equals(graveyardOwnerId) && (trigger.filter() == null
                    || TriggeredAbilityStackService.departedFilterMatches(predicateEvaluationService,
                    trigger.filter(), lastKnown, DepartedAttachments.NONE, emblemFilterContext(gameData, emblem)))) {
                return true;
            }
        }
        return false;
    }

    /** "Whenever [a permanent] is put into your graveyard from the battlefield": an owner's battlefield-to-graveyard trigger. */
    private static boolean isPutIntoYourGraveyardFromBattlefield(ZoneChangeTrigger trigger) {
        return trigger.relation() == ObjectRelation.YOU_OWN && trigger.from().equals(Set.of(Zone.BATTLEFIELD))
                && trigger.to() == Zone.GRAVEYARD;
    }

    private void putEmblemGraveyardFromBattlefieldAbilityOnStack(GameData gameData, Emblem emblem,
                                                                 TriggeredAbility ability, Card card,
                                                                 Permanent lastKnown, UUID controllerId) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : card;
        String description = emblemName(emblem);
        long graveyardEntryVersion = gameData.graveyardEntryVersion(card.getId());
        putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()), entry -> {
                    entry.setTriggeringCard(card.getId(), graveyardEntryVersion);
                    entry.setTriggeringPermanentId(lastKnown.getId());
                    entry.setTriggeringPermanent(lastKnown);
                    entry.setTriggeringPermanentControllerId(controllerId);
                    entry.setNonTargeting(true);
                }, false);
        gameLogService.append(gameData, GameLog.text(description + " triggers for " + card.getName()
                + " put into a graveyard."));
        log.info("Game {} - {} triggers for {} put into a graveyard from the battlefield", gameData.id, description,
                card.getName());
    }

    /**
     * Queues one "copy that spell" delayed trigger per unit of instant/sorcery copy-rider mana
     * (Primal Wellspring, Pyromancer's Goggles) spent on this matching spell (CR 106.6a); the copy
     * may choose new targets.
     */
    private void queueManaLinkedSpellCopyTriggers(GameData gameData, Card spellCard, UUID castingPlayerId,
                                                  int copies) {
        if (copies <= 0) return;
        StackEntry spellEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getTargetableId().equals(spellCard.getId())) {
                spellEntry = se;
                break;
            }
        }
        if (spellEntry == null) return;
        for (int copyNumber = 0; copyNumber < copies; copyNumber++) {
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(spellCard,
                    castingPlayerId,
                    "Copy " + spellCard.getName(),
                    new ArrayList<>(List.of(new CopyControllerCastSpellEffect(
                            new StackEntry(spellEntry), castingPlayerId)))
            ));
        }
        gameLogService.append(gameData, GameLog.cardThen(spellCard, " is copied."));
        log.info("Game {} - {} mana-linked spell-copy trigger(s) queued for {}",
                gameData.id, copies, spellCard.getName());
    }

    /**
     * Queues one copy trigger per unit of copy-rider mana (Sunken Palace) spent to activate this
     * non-mana ability (CR 106.6a).
     */
    public void checkManaLinkedAbilityCopyTrigger(GameData gameData, UUID activatingPlayerId,
                                                   StackEntry abilityEntry, ActivatedAbility ability) {
        ManaPool pool = gameData.playerManaPools.get(activatingPlayerId);
        int pendingCopies = pool == null ? 0 : pool.takeSpentCopyGrantingMana(ManaCopyRider.SPELL_OR_ABILITY);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
        if (abilityEntry == null) return;
        for (int i = 0; i < pendingCopies; i++) {
            CardEffect copyEffect = new CopyControllerActivatedAbilityEffect(
                    new StackEntry(abilityEntry), ability, activatingPlayerId);
            triggeredAbilityStackService.putDelayed(gameData, TriggeredAbilityStackService.madeAbility(abilityEntry.getCard(),
                    activatingPlayerId,
                    abilityEntry.getCard().getName() + "'s ability",
                    new ArrayList<>(List.of(copyEffect))
            ));
            gameLogService.append(gameData,
                    GameLog.textCardText("A copy of ", abilityEntry.getCard(), "'s ability is created."));
            log.info("Game {} - mana-linked ability copy trigger queued for {}", gameData.id,
                    abilityEntry.getCard().getName());
        }
    }

    /**
     * Copy-rider mana spent to activate a mana ability triggers, but mana abilities can't be
     * copied, so the spent rider does nothing.
     */
    public void consumeManaLinkedCopyOnManaAbility(GameData gameData, UUID playerId) {
        ManaPool pool = gameData.playerManaPools.get(playerId);
        if (pool != null) {
            pool.discardSpentCopyGrantingMana();
        }
    }

    private void collectCreatureDeathTriggerWatchers(GameData gameData, Permanent dyingPermanent) {
        for (CreatureDeathTriggerWatcher watcher : List.copyOf(gameData.creatureDeathTriggerWatchers)) {
            if (watcher.onlyOthers() && dyingPermanent.getId().equals(watcher.sourcePermanentId())) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    watcher.sourcePermanentId());
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
        for (TargetedCreatureDeathTriggerWatcher watcher
                : List.copyOf(gameData.targetedCreatureDeathTriggerWatchers)) {
            if (!watcher.watchedPermanentId().equals(dyingPermanent.getId())) {
                continue;
            }
            gameData.targetedCreatureDeathTriggerWatchers.remove(watcher);
            if (dyingPermanent.getCounterCount(watcher.counterType()) <= 0) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringPermanentId(dyingPermanent.getId());
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because its watched creature died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /** Fires delayed triggers registered for creatures damaged by a particular source object. */
    public void triggerDelayedDamagedCreatureDeathTriggers(GameData gameData, Card dyingCard,
                                                            UUID dyingCreatureControllerId) {
        if (dyingCard == null) {
            return;
        }
        UUID dyingCardId = dyingCard.getId();
        for (DamagedCreatureDeathTriggerWatcher watcher
                : List.copyOf(gameData.damagedCreatureDeathTriggerWatchers)) {
            if (!watcher.allControllers() && !watcher.controllerId().equals(dyingCreatureControllerId)
                    || !gameData.creatureCardsDamagedThisTurnBySource
                    .getOrDefault(watcher.damageSourceId(), Set.of()).contains(dyingCardId)) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())),
                    (UUID) null,
                    (UUID) null);
            entry.setTriggeringCardId(dyingCardId);
            // "Its controller" is the dying creature's last-known controller (CR 608.2h), whatever the effects are.
            entry.setTriggeringPermanentControllerId(dyingCreatureControllerId);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} triggers because a creature it damaged died",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * The emblems' "whenever a creature dies" abilities ({@code Triggers.creatureDies(ObjectRelation.ANY)}; Liliana,
     * Defiant Necromancer's "return it to the battlefield under your control at the beginning of the next end step",
     * Sephiroth, One-Winged Angel's "target opponent loses 1 life and you gain 1 life"). Abilities of emblems function
     * in the command zone (CR 114.4), so the permanent sweep never sees them. Each ability goes on the stack once as
     * one object of its effects (CR 603.3), the emblem's source card as its source, and records the creature that
     * died ("it": its card, its last-known permanent and controller) whatever its effects are; a targeted one's target
     * is chosen as it goes on the stack (CR 603.3d), restricted by the ability's own target filter. The reader judges
     * the trigger's filter on the creature as it last existed and no options; an ability with options or another
     * relation fails loudly instead of triggering wrongly. "Whenever [a permanent] is put into your graveyard from the
     * battlefield" is {@link #checkEmblemPutIntoGraveyardFromBattlefieldTriggers}'.
     */
    private void collectEmblemCreatureDeathTriggers(GameData gameData, Card dyingCard, Permanent lastKnown,
                                                     UUID dyingControllerId) {
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesCreatureDie(gameData, emblem, ability, lastKnown)) {
                    putEmblemDeathAbilityOnStack(gameData, emblem, ability, dyingCard, lastKnown, dyingControllerId);
                }
            }
        }
    }

    /** Whether {@code ability} of {@code emblem} triggers on the creature {@code lastKnown} dying. */
    private boolean emblemSeesCreatureDie(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                          Permanent lastKnown) {
        ZoneChangeTrigger trigger = creatureDeathTrigger(ability);
        if (trigger == null || isPutIntoYourGraveyardFromBattlefield(trigger)) {
            return false;
        }
        if (trigger.relation() != ObjectRelation.ANY || !ability.options().equals(TriggerOptions.DEFAULT)) {
            throw new IllegalStateException(emblemName(emblem) + " has a death ability its reader doesn't judge: "
                    + ability);
        }
        return trigger.filter() == null || lastKnown != null
                && predicateEvaluationService.matchesPermanentPredicate(lastKnown, trigger.filter(),
                FilterContext.of(gameData).withSourceControllerId(emblem.controllerId()));
    }

    private void putEmblemDeathAbilityOnStack(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                              Card dyingCard, Permanent lastKnown, UUID dyingControllerId) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : dyingCard;
        String description = emblemName(emblem);
        // "If a creature dying causes a triggered ability of an emblem you control to trigger, it triggers an
        // additional time": the stack door counts it for an emblem's creature-death ability.
        putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()), entry -> {
                    entry.setTriggeringCardId(dyingCard.getId());
                    entry.setTriggeringPermanent(lastKnown);
                    entry.setTriggeringPermanentControllerId(dyingControllerId);
                    entry.setNonTargeting(true);
                }, true);
        gameLogService.append(gameData, GameLog.text(description + " triggers (a creature died)."));
        log.info("Game {} - {} triggers for {} dying", gameData.id, description, dyingCard.getName());
    }

    /**
     * Fires the delayed "when that permanent leaves the battlefield" triggers that watch {@code target}
     * (CR 603.7): the destination-keyed {@code DelayedZoneChangeTrigger}s and the leaves-the-battlefield
     * abilities a resolving spell registered for it. The permanent's own leaves-the-battlefield abilities go
     * through the trigger pipeline.
     */
    public void checkDelayedLeavesBattlefieldTriggers(GameData gameData, Permanent target, Zone destination) {
        for (var delayed : gameData.drainDelayedActions(
                com.github.laxika.magicalvibes.model.action.DelayedZoneChangeTrigger.class,
                action -> action.watchedPermanentId().equals(target.getId()))) {
            CardEffect effect = delayed.effectsByDestination().get(destination);
            if (effect == null) continue;
            StackEntry entry = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(), delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(effect)), null, delayed.sourcePermanentId());
            entry.setTriggeringCardId(target.getOriginalCard().getId());
            if (destination == Zone.EXILE) {
                entry.setTriggeringCardExileEntryVersion(
                        gameData.findExiledCard(entry.getTriggeringCardId()) == null ? -1L
                                : gameData.exileEntryVersions.getOrDefault(entry.getTriggeringCardId(), 0L));
            }
            entry.setTriggeringCardGraveyardEntryVersion(
                    destination == Zone.GRAVEYARD ? -1L : 0L);
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().conditions().getFirst().trigger()
                    .equals(Triggers.leavesBattlefield(ObjectRelation.THIS))
                    || !target.getId().equals(watcher.watchedPermanentId())) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s delayed trigger", temporaryEffects(watcher), (UUID) null,
                    target.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }
    }

    /**
     * Fires delayed "when that creature leaves the battlefield this turn, sacrifice this creature"
     * triggers (Kjeldoran Elite Guard). Drains matching {@link DelayedSacrificeSourceWhenTargetLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} for each. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeSourceWhenTargetLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeSourceWhenTargetLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeSourceWhenTargetLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeSourceWhenTargetLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.sourcePermanentId());
            se.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (watched {} left); sacrifice source {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.sourcePermanentId());
        }
    }

    /**
     * Fires delayed "when this creature leaves the battlefield this turn, sacrifice that creature"
     * triggers (Phantasmal Mount). Drains matching {@link DelayedSacrificeTargetWhenSourceLeaves}
     * entries and enqueues a {@link SacrificeSelfEffect} against the pumped target for each. Called
     * from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedSacrificeTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedSacrificeTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedSacrificeTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedSacrificeTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            StackEntry se = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                    delayed.controllerId(),
                    delayed.sourceCard().getName() + "'s delayed trigger",
                    new ArrayList<>(List.of(new SacrificeSelfEffect())),
                    null,
                    delayed.targetPermanentId());
            se.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, se);
            gameLogService.append(gameData,
                    GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
            log.info("Game {} - {} delayed leave-trigger fires (source {} left); sacrifice target {}",
                    gameData.id, delayed.sourceCard().getName(), leavingPermanent.getCard().getName(),
                    delayed.targetPermanentId());
        }
    }

    /**
     * Fires delayed "when this artifact leaves the battlefield this turn, destroy that creature"
     * triggers (War Barge). Drains matching registrations and enqueues a non-targeting destruction
     * for each captured target. Called from every leave path in
     * {@link com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService}.
     */
    public void processDelayedDestroyTargetWhenSourceLeaves(GameData gameData, Permanent leavingPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID leavingId = leavingPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> leavingId.equals(d.watchedPermanentId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, leavingPermanent);
        }
    }

    /**
     * Fires delayed "when this creature ... becomes untapped, destroy that creature" triggers (Merieke Ri
     * Berit) for the permanent that just became untapped. Each fires only once (CR 603.7b).
     */
    public void processDelayedDestroyTargetWhenSourceUntaps(GameData gameData, Permanent untappedPermanent) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        UUID untappedId = untappedPermanent.getId();
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> d.alsoWhenUntapped() && untappedId.equals(d.watchedPermanentId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, untappedPermanent);
        }
    }

    /**
     * Fires delayed "when you lose control of this creature, exile that creature" triggers (Coffin Queen)
     * for a permanent whose control just changed away from {@code previousControllerId}. Each fires only
     * once (CR 603.7b).
     */
    public void processDelayedTargetWhenSourceLosesControl(GameData gameData, Permanent permanent,
                                                           UUID previousControllerId) {
        if (!gameData.hasDelayedAction(DelayedDestroyTargetWhenSourceLeaves.class)) {
            return;
        }
        for (DelayedDestroyTargetWhenSourceLeaves delayed : gameData.drainDelayedActions(
                DelayedDestroyTargetWhenSourceLeaves.class,
                d -> d.alsoWhenControlLost() && permanent.getId().equals(d.watchedPermanentId())
                        && previousControllerId.equals(d.controllerId()))) {
            enqueueDelayedDestroyTarget(gameData, delayed, permanent);
        }
    }

    private void enqueueDelayedDestroyTarget(GameData gameData, DelayedDestroyTargetWhenSourceLeaves delayed,
                                             Permanent watchedPermanent) {
        StackEntry se = TriggeredAbilityStackService.madeAbility(
                delayed.sourceCard(),
                delayed.controllerId(),
                delayed.sourceCard().getName() + "'s delayed trigger",
                List.of(delayed.exile()
                        ? new com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect(
                                com.github.laxika.magicalvibes.model.effect.RemoveLinkedPermanentEffect.Mode.EXILE,
                                delayed.targetPermanentId())
                        : new DestroyLinkedPermanentEffect(true, delayed.targetPermanentId())));
        se.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, se);
        gameLogService.append(gameData,
                GameLog.text(delayed.sourceCard().getName() + "'s delayed trigger triggers."));
        log.info("Game {} - {} delayed trigger fires (source {}); destroy target {}",
                gameData.id, delayed.sourceCard().getName(), watchedPermanent.getCard().getName(),
                delayed.targetPermanentId());
    }

    /**
     * Fires delayed counter triggers for cards that return from exile when their source leaves the
     * battlefield. The return controller is compared with the controller captured when the
     * source's enter-the-battlefield ability resolved.
     */
    public void processDelayedExileReturnCounterTriggers(GameData gameData, UUID leavingPermanentId,
                                                          List<PendingExileReturn> pendingReturns,
                                                          List<UUID> returnedPermanentIds) {
        List<DelayedExileReturnCounterTrigger> delayedTriggers = gameData.drainDelayedActions(
                DelayedExileReturnCounterTrigger.class,
                trigger -> leavingPermanentId.equals(trigger.watchedPermanentId()));
        if (delayedTriggers.isEmpty()) {
            return;
        }

        for (int i = 0; i < pendingReturns.size(); i++) {
            PendingExileReturn pending = pendingReturns.get(i);
            UUID returnedPermanentId = returnedPermanentIds.get(i);
            if (returnedPermanentId == null) {
                continue;
            }
            for (DelayedExileReturnCounterTrigger delayed : delayedTriggers) {
                if (!pending.controllerId().equals(delayed.controllerId())) {
                    continue;
                }
                StackEntry trigger = TriggeredAbilityStackService.madeAbility(delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed ability",
                        new ArrayList<>(List.of(new PutCounterOnTargetPermanentEffect(
                                delayed.counterType(), delayed.counterAmount()))),
                        returnedPermanentId,
                        (UUID) null);
                trigger.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, trigger);
                gameLogService.append(gameData,
                        GameLog.cardThen(delayed.sourceCard(), "'s delayed ability triggers."));
                log.info("Game {} - {} delayed return counter trigger queued for {}",
                        gameData.id, delayed.sourceCard().getName(), pending.card().getName());
            }
        }
    }

    public void triggerDelayedPoisonOnDeath(GameData gameData, UUID dyingCreatureCardId, UUID controllerId) {
        Integer poisonAmount = gameData.creatureGivingControllerPoisonOnDeathThisTurn.remove(dyingCreatureCardId);
        if (poisonAmount == null || poisonAmount <= 0) {
            return;
        }

        poisonAmount = gameQueryService.applyPoisonCounterReplacement(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;

        poisonAmount = gameQueryService.replacePoisonCounters(gameData, controllerId, poisonAmount);
        if (poisonAmount <= 0) return;
        int currentPoison = gameData.playerPoisonCounters.getOrDefault(controllerId, 0);
        gameData.playerPoisonCounters.put(controllerId, currentPoison + poisonAmount);
        gameData.rulesEvents.occur(CounterChangeEvent.ofPlayer(CounterChangeEvent.Kind.YOU_PUT_COUNTERS, controllerId,
                poisonAmount));

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " gets " + poisonAmount + " poison counter"
                + (poisonAmount > 1 ? "s" : "") + " (delayed trigger: creature died this turn).";
        gameLogService.append(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} gets {} poison counter(s) (delayed trigger: creature died this turn)",
                gameData.id, playerName, poisonAmount);
    }

    /**
     * Delayed "return that card when it dies this turn" (Graceful Reprieve, Supernatural Stamina,
     * Adarkar Valkyrie): if the dying creature's card was registered, push a triggered ability that
     * returns it from its owner's graveyard to the battlefield. Fires at most once per registration.
     */
    public void triggerDelayedReturnOnDeath(GameData gameData, UUID dyingPermanentCardId, Card graveyardCard, UUID ownerId) {
        List<DelayedReturnOnDeath> delayedReturns = gameData.creaturesReturnedToBattlefieldOnDeathThisTurn
                .remove(dyingPermanentCardId);
        if (delayedReturns == null || delayedReturns.isEmpty()) {
            return;
        }

        for (DelayedReturnOnDeath delayedReturn : delayedReturns) {
            if (delayedReturn.requireControllerGraveyard()
                    && !delayedReturn.controllerId().equals(ownerId)) {
                continue;
            }
            UUID triggerControllerId = delayedReturn.controllerId();
            Card triggerSource = delayedReturn.sourceCard() != null ? delayedReturn.sourceCard() : graveyardCard;

            StackEntry entry = TriggeredAbilityStackService.madeAbility(triggerSource,
                    triggerControllerId,
                    "Return " + graveyardCard.getName() + " to the battlefield",
                    new ArrayList<>(List.of(new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(
                            delayedReturn.enterTapped(), delayedReturn.returnUnderController())))
            );
            entry.setTriggeringCardId(graveyardCard.getId());
            entry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(graveyardCard.getId()));
            triggeredAbilityStackService.putDelayed(gameData, entry);

            gameLogService.append(gameData, GameLog.cardThen(graveyardCard, " will return to the battlefield (it died this turn)."));
            log.info("Game {} - Delayed return trigger: {} will return to the battlefield", gameData.id, graveyardCard.getName());
        }
    }

    /**
     * Resolves delayed effects registered for a permanent dying this turn. Creature-only registrations
     * and registrations that can also apply to other permanent types are kept separately, then combined
     * when the permanent dies. {@code dyingPermanent}, the permanent as it last existed on the battlefield
     * (CR 603.10a), is each entry's triggering permanent.
     */
    public void triggerDelayedEffectOnDeath(GameData gameData, UUID dyingPermanentCardId,
                                            UUID dyingPermanentControllerId, int dyingPermanentPower,
                                            int dyingPermanentManaValue, Permanent dyingPermanent) {
        UUID dyingPermanentId = dyingPermanent.getId();
        List<DelayedEffectOnDeath> registrations = new ArrayList<>();
        List<DelayedEffectOnDeath> creatureRegistrations =
                gameData.creatureTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (creatureRegistrations != null) {
            registrations.addAll(creatureRegistrations);
        }
        List<DelayedEffectOnDeath> permanentRegistrations =
                gameData.permanentTriggeringEffectOnDeathThisTurn.remove(dyingPermanentCardId);
        if (permanentRegistrations != null) {
            registrations.addAll(permanentRegistrations);
        }
        List<DelayedEffectOnDeath> graveyardRegistrations =
                gameData.permanentTriggeringEffectOnGraveyardEntry.remove(dyingPermanentCardId);
        if (graveyardRegistrations != null) {
            registrations.addAll(graveyardRegistrations);
        }
        if (registrations.isEmpty()) {
            return;
        }

        for (DelayedEffectOnDeath registration : registrations) {
            if (registration.targetPermanentId() != null
                    && !registration.targetPermanentId().equals(dyingPermanentId)) continue;

            CardEffect effect = registration.effect();
            // A delayed trigger's "if" (CR 603.7c: it is written as one effect) is checked as it triggers and again
            // as it resolves.
            if (effect instanceof ConditionalEffect conditional
                    && !conditionEvaluationService.isMet(gameData, conditional.condition(),
                    ConditionContext.forCard(registration.sourceCard(), registration.controllerId()))) {
                continue;
            }
            StackEntry delayedEntry = TriggeredAbilityStackService.madeAbility(registration.sourceCard(),
                    registration.controllerId(),
                    registration.sourceCard().getName() + " triggers (a creature dealt damage this way died)",
                    List.of(effect), dyingPermanentControllerId, registration.sourcePermanentId());
            delayedEntry.setEventValue(Math.max(0, dyingPermanentPower));
            delayedEntry.setDyingPermanentManaValue(Math.max(0, dyingPermanentManaValue));
            delayedEntry.setTriggeringCardId(dyingPermanentCardId);
            delayedEntry.setTriggeringCardGraveyardEntryVersion(
                    gameData.graveyardEntryVersion(dyingPermanentCardId));
            delayedEntry.setTriggeringPermanent(dyingPermanent);
            if (effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    || effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                // A target of its own is chosen as it goes on the stack (CR 603.3d), not the dying creature's
                // controller.
                delayedEntry.setTargetId(null);
                triggeredAbilityStackService.putMade(gameData, delayedEntry);
                continue;
            }

            triggeredAbilityStackService.putDelayed(gameData, delayedEntry);

            log.info("Game {} - Delayed death trigger: {} triggers (a creature it damaged died this turn)",
                    gameData.id, registration.sourceCard().getName());
        }
    }

    /**
     * What a creature entering under {@code controllerId}'s control triggers besides the card abilities the trigger
     * pipeline matches and the emblems' abilities ({@link #checkEmblemEntersTriggers}): evolve (CR 702.100d), the
     * watchers and boons a resolved spell left, and the "whenever a creature you control enters" abilities a spell
     * registered until end of turn.
     */
    public void checkAllyCreatureEntersTriggers(GameData gameData, UUID controllerId, Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        for (Permanent perm : new ArrayList<>(battlefield)) {
            if (perm.isLosesAllAbilitiesUntilEndOfTurn() || perm.isFaceDown() || perm.getCard() == enteringCreature) {
                continue;
            }
            if (enteringPermanent != null && gameQueryService.hasKeyword(gameData, perm, Keyword.EVOLVE)) {
                // CR 702.100d: each instance of evolve (printed or granted) triggers separately.
                int evolveInstances = gameQueryService.triggerKeywordInstances(gameData, perm, Keyword.EVOLVE);
                for (int i = 0; i < evolveInstances; i++) {
                    putEvolveAbility(gameData, controllerId, perm, enteringPermanent);
                }
            }
        }

        for (CreatureEntersTriggerWatcher watcher
                : List.copyOf(gameData.allyCreatureEntersTriggerWatchers)) {
            if (!watcher.controllerId().equals(controllerId) || enteringPermanent == null) {
                continue;
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(),
                    watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(watcher.effect())));
            entry.setTargetId(enteringPermanent.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
        }

        if (enteringPermanent != null && !gameData.deferCreatureEntersBoons) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), true);
        }

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL))
                    || !watcher.controllerId().equals(controllerId)) {
                continue;
            }

            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary ally creature-enters trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /**
     * Whether a temporary global ability has a "whenever a creature [an opponent controls] enters" trigger condition,
     * one that narrows it down by a further filter ("a nontoken creature an opponent controls") included, which
     * {@code entering} matches as it entered.
     */
    private boolean watchesOpponentCreatureEntering(GameData gameData, TemporaryGlobalTriggeredAbility watcher,
                                                    Permanent entering) {
        for (TriggerCondition condition : watcher.ability().conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger) || !trigger.entersBattlefield()
                    || trigger.relation() != ObjectRelation.OPPONENT_CONTROLS
                    || !Triggers.CREATURE.equals(narrowedFrom(trigger.filter()))) {
                continue;
            }
            if (predicateEvaluationService.matchesPermanentPredicate(entering, trigger.filter(),
                    FilterContext.of(gameData).withSourceControllerId(watcher.controllerId())
                            .withSourceCardId(watcher.sourceCard().getId()))) {
                return true;
            }
        }
        return false;
    }

    /** The filter {@code filter} narrows: the first of an all-of filter's parts, or the filter itself. */
    private static PermanentPredicate narrowedFrom(PermanentPredicate filter) {
        return filter instanceof PermanentAllOfPredicate allOf && !allOf.predicates().isEmpty()
                ? allOf.predicates().getFirst() : filter;
    }

    private void collectCreatureSpellBoonTriggers(GameData gameData, Card spellCard,
                                                 UUID castingPlayerId, boolean castFaceDown) {
        if (!castFaceDown && !spellCard.hasType(CardType.CREATURE)) return;
        for (Boon boon : List.copyOf(gameData.boons)) {
            if (boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_CAST
                    || !castingPlayerId.equals(boon.controllerId())) continue;
            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(boon.withRemainingUses(boon.remainingUses() - 1));
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(),
                    boon.controllerId(), boon.sourceCard().getName() + "'s boon",
                    new ArrayList<>(List.of(boon.effect())));
            entry.setTriggeringCardId(spellCard.getId());
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }
    }

    /**
     * Queues the controller's boons that fire when permanents enter. {@link BoonTrigger#CREATURE_ENTERS}
     * boons watch creatures only; {@link BoonTrigger#PERMANENT_ENTERS} boons watch every permanent, so the
     * creature entries ({@code creatureEntries}) and the noncreature entries are offered separately.
     */
    private void collectAllyBoonTriggers(GameData gameData, UUID controllerId,
                                         List<UUID> enteringPermanentIds, boolean creatureEntries) {
        if (enteringPermanentIds == null || enteringPermanentIds.isEmpty()) return;

        for (Boon boon : List.copyOf(gameData.boons)) {
            if ((boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_ENTERS
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.PERMANENT_ENTERS)
                    || !boon.controllerId().equals(controllerId)) continue;

            Permanent enteringPermanent = null;
            CardEffect resolved = null;
            List<UUID> matchingEnteringIds = new ArrayList<>();
            for (UUID enteringPermanentId : enteringPermanentIds) {
                Permanent candidate = gameQueryService.findPermanentById(gameData, enteringPermanentId);
                if (candidate == null) continue;
                boolean candidateIsCreature = gameQueryService.isCreature(gameData, candidate);
                if ((boon.trigger() == com.github.laxika.magicalvibes.model.BoonTrigger.CREATURE_ENTERS
                        || boon.trigger() == com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER)
                        ? !candidateIsCreature
                        : candidateIsCreature != creatureEntries) {
                    continue;
                }
                // "Whenever one or more Merfolk enter under your control": the boon's filter on the entering card.
                if (boon.filter() != null && !predicateEvaluationService.matchesCardPredicate(candidate.getCard(),
                        boon.filter(), boon.sourceCard().getId(), gameData, controllerId)) {
                    continue;
                }
                if (enteringPermanent == null) {
                    enteringPermanent = candidate;
                    resolved = boon.effect();
                }
                matchingEnteringIds.add(candidate.getId());
            }
            if (enteringPermanent == null || resolved == null) continue;

            if (matchingEnteringIds.size() > 1 && boon.targetFilter() == null
                    && boon.trigger() != com.github.laxika.magicalvibes.model.BoonTrigger.ONE_OR_MORE_CREATURES_ENTER
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Whenever a creature you control enters, [do something to] it": every creature that entered
                // together triggers the boon separately and consumes its own use.
                gameData.boons.remove(boon);
                int usesLeft = boon.remainingUses();
                for (UUID matchingId : matchingEnteringIds) {
                    if (usesLeft <= 0) break;
                    Permanent matching = gameQueryService.findPermanentById(gameData, matchingId);
                    if (matching == null) continue;
                    usesLeft--;
                    StackEntry perCreatureEntry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(),
                            boon.controllerId(), boon.sourceCard().getName() + "'s boon", List.of(resolved));
                    perCreatureEntry.setTargetId(matching.getId());
                    perCreatureEntry.setTriggeringPermanentId(matching.getId());
                    perCreatureEntry.setTriggeringCardId(matching.getCard().getId());
                    perCreatureEntry.setNonTargeting(true);
                    triggeredAbilityStackService.putDelayed(gameData, perCreatureEntry);
                    gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
                }
                if (usesLeft > 0) {
                    gameData.boons.add(boon.withRemainingUses(usesLeft));
                }
                continue;
            }

            gameData.boons.remove(boon);
            if (boon.remainingUses() > 1) {
                gameData.boons.add(boon.withRemainingUses(boon.remainingUses() - 1));
            }

            Permanent sourcePermanent = findPermanentByCard(gameData, boon.sourceCard());
            // "attached to one of them": when several creatures entered together the controller picks which.
            TargetFilter boonTargetFilter = boon.targetFilter();
            if (matchingEnteringIds.size() > 1
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                    && (boonTargetFilter == null || boonTargetFilter instanceof PermanentPredicateTargetFilter)) {
                PermanentPredicate entered = new PermanentAnyOfPredicate(matchingEnteringIds.stream()
                        .<PermanentPredicate>map(PermanentIsSpecificPermanentPredicate::new).toList());
                boonTargetFilter = new PermanentPredicateTargetFilter(
                        boonTargetFilter instanceof PermanentPredicateTargetFilter existing
                                ? new PermanentAllOfPredicate(List.of(existing.predicate(), entered)) : entered,
                        "Target must be one of the creatures that entered");
            }
            StackEntry entry = TriggeredAbilityStackService.madeAbility(boon.sourceCard(), boon.controllerId(),
                    boon.sourceCard().getName() + "'s boon", List.of(resolved), enteringPermanent.getId(),
                    sourcePermanent == null ? null : sourcePermanent.getId());
            entry.setTriggeringPermanentId(enteringPermanent.getId());
            entry.setTriggeringPermanentControllerId(controllerId);
            entry.setTriggeringCardId(enteringPermanent.getCard().getId());
            entry.rememberLastKnownPermanentCard(enteringPermanent.getId(), enteringPermanent.getCard());
            entry.setNonTargeting(true);
            if (boonTargetFilter != null
                    && resolved.targetSpec().admits(TargetPredicate.Kind.PERMANENT)) {
                // "Attached to one of them": its target, restricted by the boon's filter, is chosen as it goes on the
                // stack (CR 603.3d).
                entry.setTargetFilter(boonTargetFilter);
                triggeredAbilityStackService.putMade(gameData, entry);
            } else {
                triggeredAbilityStackService.putDelayed(gameData, entry);
            }
            gameLogService.append(gameData, GameLog.abilityTriggers(boon.sourceCard()));
        }
    }

    /**
     * Fires turn-scoped delayed triggers registered by Beck. Each registration is a separate
     * optional draw trigger, including for tokens and creatures controlled by the other player.
     */
    public void checkCreatureEntersThisTurnTriggers(GameData gameData, UUID enteringCreatureControllerId,
                                                    Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.creatureEntersDrawSourcesThisTurn.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                // The delayed ability the spell made ("whenever a creature enters this turn, you may draw a card"),
                // its "you may" asked as it resolves (CR 603.5).
                StackEntry entry = TriggeredAbilityStackService.madeAbility(sourceCard, controllerId,
                        sourceCard.getName() + "'s ability",
                        List.of(new MayEffect(new DrawCardEffect(), "Draw a card?")));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
            }
        }
    }

    /**
     * The abilities a spell registered until end of turn that see a creature or an enchantment enter under their
     * controller's control.
     */
    public void checkAnyPermanentEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        if (!enteringPermanentHasType(gameData, enteringCard, CardType.CREATURE)
                && !enteringPermanentHasType(gameData, enteringCard, CardType.ENCHANTMENT)) {
            return;
        }
        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watcher.ability().hasTrigger(Triggers.enters(Triggers.CREATURE_OR_ENCHANTMENT,
                    ObjectRelation.YOU_CONTROL)) || !watcher.controllerId().equals(enteringControllerId)) {
                continue;
            }

            StackEntry entry = TriggeredAbilityStackService.madeAbility(watcher.sourceCard(), watcher.controllerId(),
                    watcher.sourceCard().getName() + "'s ability", temporaryEffects(watcher));
            entry.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, entry);
            gameLogService.append(gameData, GameLog.abilityTriggers(watcher.sourceCard()));
            log.info("Game {} - {} temporary creature-or-enchantment-enter trigger fires",
                    gameData.id, watcher.sourceCard().getName());
        }
    }

    /** The boons a noncreature permanent entering under {@code controllerId}'s control uses. */
    public void checkAllyPermanentEntersTriggers(GameData gameData, UUID controllerId, Card enteringCard) {
        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCard);
        if (enteringPermanent != null && !gameQueryService.isCreature(gameData, enteringPermanent)) {
            collectAllyBoonTriggers(gameData, controllerId, List.of(enteringPermanent.getId()), false);
        }
    }

    /** The abilities a spell registered until end of turn that see a creature enter under an opponent's control. */
    public void checkOpponentCreatureEntersTriggers(GameData gameData, UUID enteringCreatureControllerId, Card enteringCreature) {
        if (!enteringPermanentHasType(gameData, enteringCreature, CardType.CREATURE)) return;

        Permanent enteringPermanent = findPermanentByCard(gameData, enteringCreature);
        if (enteringPermanent == null) return;

        for (TemporaryGlobalTriggeredAbility watcher : List.copyOf(gameData.temporaryGlobalTriggeredAbilities)) {
            if (!watchesOpponentCreatureEntering(gameData, watcher, enteringPermanent)
                    || watcher.controllerId().equals(enteringCreatureControllerId)) {
                continue;
            }

            List<CardEffect> resolvedEffects = temporaryEffects(watcher);

            // "A copy of that creature" (Theoretical Duplication) is the stack entry's triggering permanent.
            enqueueTemporaryGlobalTrigger(gameData, watcher, resolvedEffects, enteringPermanent.getId(), 0,
                    enteringPermanent.getId());
        }
    }

    /**
     * Puts Judgment of Alexander's "whenever damage from a creature is prevented this way, each commander creature you
     * control deals damage equal to its power to that creature" on the stack: a delayed triggered ability the spell made
     * as it resolved, with the spell as its source and its controller, the protected player, as its controller
     * (CR 603.7d). "That creature" is the creature whose damage was prevented, which it doesn't target.
     */
    public void checkCommanderDamagePreventedTriggers(GameData gameData, UUID damagedPlayerId,
                                                       UUID preventedCreatureId, Card sourceCard) {
        if (damagedPlayerId == null || sourceCard == null) return;

        StackEntry entry = TriggeredAbilityStackService.madeAbility(sourceCard, damagedPlayerId,
                sourceCard.getName() + "'s triggered ability",
                List.of(new CommanderCreaturesDealPowerDamageToPreventedCreatureEffect()), preventedCreatureId,
                (UUID) null);
        entry.setNonTargeting(true);
        triggeredAbilityStackService.putDelayed(gameData, entry);
        gameLogService.append(gameData, GameLog.abilityTriggers(sourceCard));
    }

    /** Fires Full Throttle's delayed untap trigger once for each registration at each combat. */
    public void checkUntapAttackedCreaturesEachCombatThisTurnTriggers(GameData gameData) {
        List<UUID> controllers = new ArrayList<>(gameData.orderedPlayerIds);
        int activePlayerIndex = controllers.indexOf(gameData.activePlayerId);
        if (activePlayerIndex > 0) {
            List<UUID> apnapControllers = new ArrayList<>(controllers.subList(activePlayerIndex, controllers.size()));
            apnapControllers.addAll(controllers.subList(0, activePlayerIndex));
            controllers = apnapControllers;
        }

        for (UUID controllerId : controllers) {
            List<Card> sources = gameData.untapAttackedCreaturesEachCombatThisTurnSources.get(controllerId);
            if (sources == null) continue;
            for (Card sourceCard : new ArrayList<>(sources)) {
                StackEntry entry = TriggeredAbilityStackService.madeAbility(sourceCard,
                        controllerId,
                        sourceCard.getName() + "'s delayed ability",
                        List.of(new UntapPermanentsEffect(TapUntapScope.ATTACKED_CREATURES)));
                entry.setNonTargeting(true);
                triggeredAbilityStackService.putDelayed(gameData, entry);
                gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                        "'s delayed ability triggers."));
                log.info("Game {} - {} delayed combat trigger queued", gameData.id, sourceCard.getName());
            }
        }
    }

    /**
     * The emblems' "whenever [a filter permanent] you control enters" abilities for {@code enteringCard}, which entered
     * under {@code enteringControllerId}'s control (Lukka, Wayward Bonder's, Chandra, Spark Hunter's, Koth, Fire of
     * Resistance's, Kiora, Master of the Depths' and Nissa, Vital Force's emblems). Abilities of emblems function in
     * the command zone (CR 114.4); an emblem isn't a permanent (CR 110.1), so an effect that makes a triggered ability
     * of a permanent trigger an additional time (Ancient Greenwarden, Panharmonicon) doesn't apply to it. Each ability
     * whose trigger's filter the permanent matches as it is now goes on the stack once as one object of its effects
     * (CR 603.3), the emblem's source card as its source and the entering permanent as its triggering permanent
     * ("it", read as the ability resolves); a targeted ability's target is chosen as it goes on the stack
     * (CR 603.3d), and a "you may" is asked as it resolves (CR 603.5). The reader judges no options, causes, origins
     * or turn; an ability with one fails loudly instead of triggering wrongly.
     */
    public void checkEmblemEntersTriggers(GameData gameData, UUID enteringControllerId, Card enteringCard) {
        if (gameData.emblems.isEmpty()) return;
        Permanent entering = findPermanentByCard(gameData, enteringCard);
        if (entering == null) return;
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (TriggeredAbility ability : emblem.triggeredAbilities()) {
                if (emblemSeesEntering(gameData, emblem, ability, enteringControllerId, entering)) {
                    putEmblemAbilityOnStack(gameData, emblem, ability, entering, enteringControllerId, "entering");
                }
            }
        }
    }

    /**
     * Whether {@code ability} of {@code emblem} triggers on {@code entering} entering under
     * {@code enteringControllerId}'s control.
     */
    private boolean emblemSeesEntering(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                       UUID enteringControllerId, Permanent entering) {
        for (TriggerCondition condition : ability.conditions()) {
            if (!(condition.trigger() instanceof ZoneChangeTrigger trigger) || !trigger.entersBattlefield()) {
                continue;
            }
            if (!trigger.from().isEmpty() || !trigger.excludedTo().isEmpty() || !trigger.causes().isEmpty()
                    || trigger.causedBy() != com.github.laxika.magicalvibes.model.trigger.PlayerRelation.ANY
                    || trigger.duringYourTurn() || !ability.options().equals(TriggerOptions.DEFAULT)) {
                throw new IllegalStateException(emblemName(emblem) + " has an enters ability its reader doesn't "
                        + "judge: " + ability);
            }
            if (controlledByRelatedPlayer(trigger.relation(), emblem, enteringControllerId)
                    && (trigger.filter() == null || predicateEvaluationService.matchesPermanentPredicate(entering,
                    trigger.filter(), emblemFilterContext(gameData, emblem)))) {
                return true;
            }
        }
        return false;
    }

    /** Whether a permanent {@code controllerId} controls relates to {@code emblem} as {@code relation} says. */
    private static boolean controlledByRelatedPlayer(ObjectRelation relation, Emblem emblem, UUID controllerId) {
        return switch (relation) {
            case YOU_CONTROL, ANOTHER_YOU_CONTROL -> emblem.controllerId().equals(controllerId);
            case OPPONENT_CONTROLS -> !emblem.controllerId().equals(controllerId);
            case ANY, ANOTHER -> true;
            default -> throw new IllegalStateException(emblemName(emblem) + " has a trigger relation its reader "
                    + "doesn't judge: " + relation);
        };
    }

    private static FilterContext emblemFilterContext(GameData gameData, Emblem emblem) {
        FilterContext context = FilterContext.of(gameData).withSourceControllerId(emblem.controllerId());
        return emblem.sourceCard() == null ? context : context.withSourceCardId(emblem.sourceCard().getId());
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
     * Puts {@code ability} of {@code emblem} on the stack for the event {@code triggering} was about, as its triggering
     * permanent: a targeted ability through its target choice, which keeps the triggering permanent, any other one as
     * a non-targeting entry. An emblem without a source card (one a test built) is described as "Emblem's emblem",
     * with the triggering permanent's card as its source.
     */
    private void putEmblemAbilityOnStack(GameData gameData, Emblem emblem, TriggeredAbility ability,
                                         Permanent triggering, UUID triggeringControllerId, String event) {
        Card source = emblem.sourceCard() != null ? emblem.sourceCard() : triggering.getCard();
        String description = emblemName(emblem);
        putEmblemAbility(gameData, emblem, ability, source, description,
                ConditionContext.forCard(source, emblem.controllerId()).withTriggeringPermanentId(triggering.getId()),
                entry -> {
                    entry.setTriggeringPermanentId(triggering.getId());
                    entry.setTriggeringPermanentControllerId(triggeringControllerId);
                    entry.setTriggeringCardId(triggering.getCard().getId());
                    entry.setNonTargeting(true);
                }, false);
        gameLogService.append(gameData, GameLog.text(description + " triggers for " + triggering.getCard().getName()
                + " " + event + "."));
        log.info("Game {} - {} triggers for {} {}", gameData.id, description, triggering.getCard().getName(), event);
    }

    /**
     * Fires token-entry abilities for a token-creation batch. Ordinary abilities use one trigger
     * for the batch, while abilities that branch on their resolution count receive one trigger for
     * each token in the batch.
     */
    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        checkAllyTokenEntersTriggers(gameData, controllerId, count, List.of());
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        checkAllyTokenEntersTriggers(gameData, controllerId, permanentIds.size(), permanentIds);
    }

    /** The boons the tokens entering under {@code controllerId}'s control use. */
    private void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count,
                                               List<UUID> permanentIds) {
        if (count <= 0) return;
        collectAllyBoonTriggers(gameData, controllerId, permanentIds, true);
    }

    /** The exiled voyages that see a land enter under its controller's control. */
    public void checkAllyLandEntersTriggers(GameData gameData, UUID landControllerId, Card enteringLand) {
        collectExiledVoyageTriggers(gameData, landControllerId);
    }

    private void collectExiledVoyageTriggers(GameData gameData, UUID landControllerId) {
        for (var exiled : new ArrayList<>(gameData.exiledCards)) {
            UUID cardId = exiled.card().getId();
            UUID controllerId = gameData.exiledVoyageControllerIds.get(cardId);
            if (!gameData.exiledVoyageCounters.containsKey(cardId)) {
                continue;
            }
            if (gameData.findExiledCard(cardId) == null || controllerId == null) {
                gameData.exiledVoyageCounters.remove(cardId);
                gameData.exiledVoyageControllerIds.remove(cardId);
                continue;
            }
            if (!landControllerId.equals(controllerId)) {
                continue;
            }

            StackEntry voyage = TriggeredAbilityStackService.madeAbility(exiled.card(), controllerId,
                    exiled.card().getName() + "'s ability", List.of(new MayEffect(
                            new PutVoyageCounterOnExiledCardEffect(),
                            "Put a voyage counter on Cosima?",
                            new ReturnVoyagingCardFromExileEffect())));
            voyage.setNonTargeting(true);
            triggeredAbilityStackService.putDelayed(gameData, voyage);
            gameLogService.append(gameData, GameLog.abilityTriggers(exiled.card()));
            log.info("Game {} - {} exiled voyage landfall trigger queued", gameData.id,
                    exiled.card().getName());
        }
    }

    /**
     * Puts one instance of {@code source}'s evolve (CR 702.100a) on the stack through the stack door for
     * {@code enteringPermanent}, a creature that entered under its controller's control: the door judges its
     * intervening "if" as it triggers and wraps its effect so it is checked again as it resolves (CR 603.4), and counts
     * the additional times it triggers for the entering creature (an entering creature causing it, Panharmonicon).
     */
    private void putEvolveAbility(GameData gameData, UUID controllerId, Permanent source,
                                  Permanent enteringPermanent) {
        if (gameQueryService.areOpponentPermanentETBTriggersSuppressed(gameData, controllerId)) {
            return;
        }
        UUID enteringId = enteringPermanent.getId();
        boolean triggered = triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(EVOLVE_ABILITY,
                source.getCard(), source, controllerId, source.getCard().getName() + "'s ability",
                ConditionContext.forPermanent(source, controllerId).withTriggeringPermanentId(enteringId),
                entry -> {
                    entry.setTriggeringPermanentId(enteringId);
                    entry.setNonTargeting(true);
                }).withCause(new TriggerCause.Entered(enteringPermanent.getCard(), controllerId, enteringId)));
        if (triggered) {
            gameLogService.append(gameData, GameLog.abilityTriggers(source.getCard()));
        }
    }

    private Permanent findPermanentByCard(GameData gameData, Card card) {
        Permanent[] found = new Permanent[1];
        gameData.forEachPermanent((playerId, permanent) -> {
            if (found[0] == null && permanent.getCard() == card) {
                found[0] = permanent;
            }
        });
        return found[0];
    }

    private boolean enteringPermanentHasType(GameData gameData, Card card, CardType type) {
        Permanent permanent = findPermanentByCard(gameData, card);
        if (permanent == null) return card.hasType(type);
        return switch (type) {
            case CREATURE -> gameQueryService.isCreature(gameData, permanent);
            case ENCHANTMENT -> gameQueryService.isEnchantment(gameData, permanent);
            default -> card.hasType(type);
        };
    }

    /** Evolve's intervening "if" (CR 702.100a): the entering creature is bigger in power or toughness. */
    private static final Condition EVOLVE_COMPARISON = new TriggeringPermanentHasGreaterPowerOrToughnessThanSource();

    /**
     * Evolve (CR 702.100a): "Whenever a creature you control enters, if that creature's power is greater than this
     * creature's power and/or that creature's toughness is greater than this creature's toughness, put a +1/+1 counter
     * on this creature." The keyword's ability, which no card writes; each instance triggers separately (CR 702.100d).
     */
    private static final TriggeredAbility EVOLVE_ABILITY = new TriggeredAbility(
            Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
            TriggerOptions.DEFAULT.withInterveningIf(EVOLVE_COMPARISON), new EvolveEffect());

    /**
     * Increment keyword. For each permanent the casting player controls with the {@link Keyword#INCREMENT}
     * keyword, fire the trigger if the mana spent on the cast spell is greater than that creature's current
     * power or toughness (the intervening-if; re-checked again at resolution per CR 603.4): the ability goes to the
     * stack door whole, which counts the times it triggers additionally. The mana spent is recorded as the entry's
     * {@code xValue} for the resolution handler.
     */
    private void collectIncrementTriggers(GameData gameData, Card spellCard, UUID castingPlayerId) {
        int manaSpent = gameData.getSpellCastManaSpent(spellCard.getId());
        gameData.forEachPermanent((playerId, perm) -> {
            if (!playerId.equals(castingPlayerId)) return;
            if (perm.isLosesAllAbilitiesUntilEndOfTurn()) return;
            if (!gameQueryService.hasKeyword(gameData, perm, Keyword.INCREMENT)) return;
            if (!gameQueryService.isCreature(gameData, perm)) return;
            if (manaSpent <= gameQueryService.getEffectivePower(gameData, perm)
                    && manaSpent <= gameQueryService.getEffectiveToughness(gameData, perm)) return;

            triggeredAbilityStackService.put(gameData, new TriggeredAbilityRequest(INCREMENT_ABILITY, perm.getCard(),
                    perm, castingPlayerId, perm.getCard().getName() + "'s ability",
                    ConditionContext.forPermanent(perm, castingPlayerId), entry -> entry.setXValue(manaSpent)));
            log.info("Game {} - {} increment trigger queued (mana spent {})",
                    gameData.id, perm.getCard().getName(), manaSpent);
        });
    }

    /**
     * Evaluates a predicate built only from card types against an explicit set of card types, or
     * returns {@code null} when the predicate looks at anything else.
     */
    private static Boolean matchesCardTypesOnly(CardPredicate predicate, Set<CardType> cardTypes) {
        if (predicate instanceof CardTypePredicate type) {
            return cardTypes.contains(type.cardType());
        }
        if (predicate instanceof CardNotPredicate not) {
            Boolean inner = matchesCardTypesOnly(not.predicate(), cardTypes);
            return inner == null ? null : !inner;
        }
        if (predicate instanceof CardAllOfPredicate allOf) {
            boolean result = true;
            for (CardPredicate part : allOf.predicates()) {
                Boolean partMatch = matchesCardTypesOnly(part, cardTypes);
                if (partMatch == null) return null;
                result &= partMatch;
            }
            return result;
        }
        if (predicate instanceof CardAnyOfPredicate anyOf) {
            boolean result = false;
            for (CardPredicate part : anyOf.predicates()) {
                Boolean partMatch = matchesCardTypesOnly(part, cardTypes);
                if (partMatch == null) return null;
                result |= partMatch;
            }
            return result;
        }
        return null;
    }

}
