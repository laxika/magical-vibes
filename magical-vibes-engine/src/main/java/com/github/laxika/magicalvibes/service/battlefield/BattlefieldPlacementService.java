package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingMysticReflection;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfUnchosenParityEnterTappedEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EnterBattlefieldOnDiscardEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesHaveRiotEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithSourcePowerCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledPermanentEntryReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EntryCostReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherPermanentsWithSameNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.UncastEnteringCreatureExileSupport;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AscendEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.TokenCreationReplacementSupport;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BattlefieldPlacementService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentCopierService permanentCopierService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private AscendEffectHandler ascendEffectHandler;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;
    private com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeAllPermanentsAsEntersEffectHandler sacrificeAllPermanentsAsEntersEffectHandler;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;

    public BattlefieldPlacementService(GameQueryService gameQueryService,
                                       GameLogService gameLogService,
                                       PlayerInputService playerInputService,
                                       PermanentCopierService permanentCopierService,
                                       @Lazy TriggerCollectionService triggerCollectionService,
                                       AmountEvaluationService amountEvaluationService,
                                       ConditionEvaluationService conditionEvaluationService,
                                       PredicateEvaluationService predicateEvaluationService,
                                       @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport,
                                       com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService,
                                       @Lazy PermanentRemovalService permanentRemovalService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.permanentCopierService = permanentCopierService;
        this.triggerCollectionService = triggerCollectionService;
        this.amountEvaluationService = amountEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.permanentCounterSupport = permanentCounterSupport;
        this.graveyardService = graveyardService;
        this.permanentRemovalService = permanentRemovalService;
    }

    @Autowired
    void setAscendEffectHandler(@Lazy AscendEffectHandler handler) {
        this.ascendEffectHandler = handler;
    }

    @Autowired
    void setSacrificeAllPermanentsAsEntersEffectHandler(
            @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeAllPermanentsAsEntersEffectHandler handler) {
        this.sacrificeAllPermanentsAsEntersEffectHandler = handler;
    }

    public void place(GameData gameData, BattlefieldEntryRequest request) {
        UUID controllerId = request.controllerId();
        Permanent permanent = request.permanent();
        Set<CardType> enterTappedTypes = request.enterTappedTypes();
        List<Permanent> simultaneouslyEntered = request.simultaneouslyEntered();
        int xValue = request.xValue();
        boolean kicked = request.kicked();
        List<String> repeatedAdditionalCosts = request.repeatedAdditionalCosts();
        EnterBattlefieldOnDiscardEffect discardReplacement = request.discardReplacement();
        controllerId = resolveEnteringController(gameData, controllerId, permanent);
        TokenCreationReplacementSupport.replaceCreatureTokenIfApplicable(gameData, controllerId, permanent);
        applyMysticReflectionReplacement(gameData, permanent, simultaneouslyEntered);
        Map<CounterType, Integer> countersBeforeEntry = new EnumMap<>(permanent.getCounters());
        int counterCountBeforeEntry = permanent.getCounters().values().stream().mapToInt(Integer::intValue).sum();
        int plusOnePlusOneCountersBeforeEntry = permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (applyExileUncastEnteringCreature(gameData, controllerId, permanent)) {
            return;
        }
        if (!applyRequiredGraveyardExileReplacement(gameData, controllerId, permanent, xValue)) {
            return;
        }
        if (!applyEntryCostReplacement(gameData, controllerId, permanent)) {
            return;
        }
        applySacrificeOtherPermanentsWithSameName(gameData, controllerId, permanent);
        Map<UUID, List<Permanent>> hidden = hideSimultaneouslyEntered(gameData, simultaneouslyEntered);
        RevealSubtypeOrEntersTappedEffect conditionalRevealEffect = null;
        try {
            if (sacrificeAllPermanentsAsEntersEffectHandler != null) {
                sacrificeAllPermanentsAsEntersEffectHandler.applyIfPresent(gameData, controllerId, permanent);
            }
            carrySpellTextReplacements(gameData, permanent);
            carrySpellColorOverride(gameData, controllerId, permanent);
            applyCreaturesEnterAsCopyReplacementEffect(gameData, controllerId, permanent);
            conditionalRevealEffect = findActiveConditionalRevealEffect(gameData, controllerId, permanent);
            applyEnterTappedEffects(permanent, enterTappedTypes);
            applySelfEnterTapped(permanent);
            applyConditionalEnterTapped(gameData, controllerId, permanent);
            applyAllPermanentsEnterTapped(gameData, permanent);
            applyOpponentOnlyEnterTappedEffects(gameData, controllerId, permanent);
            applyUnchosenParityEnterTapped(gameData, permanent);
            applyEnterWithCounters(gameData, controllerId, permanent, xValue, kicked, repeatedAdditionalCosts);
            applyDiscardEntryCounters(gameData, controllerId, permanent, discardReplacement);
            applyGraveyardEnterWithAdditionalCounters(gameData, controllerId, permanent, simultaneouslyEntered);
            applyControlledPermanentEntryReplacements(gameData, controllerId, permanent);
            applyControlledCreaturesEnterWithAdditionalCounters(gameData, controllerId, permanent, simultaneouslyEntered);
            applyAdditionalEnterCountersThisTurn(gameData, controllerId, permanent);
            applyControlledCreaturesEnterWithSourcePowerCounters(gameData, controllerId, permanent);
        } finally {
            restoreHiddenBattlefields(gameData, hidden);
        }
        int counterCountAfterEntry = permanent.getCounters().values().stream().mapToInt(Integer::intValue).sum();
        int plusOnePlusOneCountersAfterEntry = permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (counterCountAfterEntry > counterCountBeforeEntry && gameQueryService.isCreature(gameData, permanent)) {
            gameData.playersWhoPutCountersOnCreaturesThisTurn.add(controllerId);
        }
        if (plusOnePlusOneCountersAfterEntry > plusOnePlusOneCountersBeforeEntry
                || (plusOnePlusOneCountersBeforeEntry > 0 && plusOnePlusOneCountersAfterEntry > 0)) {
            permanentCounterSupport.recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
                    gameData, permanent, controllerId);
        }
        // CR 613.7d: an object receives its timestamp as it enters a zone.
        permanent.setTimestamp(gameData.nextTimestamp());
        gameData.playerBattlefields.get(controllerId).add(permanent);
        if (ascendEffectHandler != null) {
            ascendEffectHandler.checkPermanentAscend(gameData, controllerId);
        }
        int countersPlacedOnEntry = counterCountAfterEntry - counterCountBeforeEntry;
        if (countersPlacedOnEntry > 0) {
            triggerCollectionService.checkYouPutCountersTriggers(gameData, controllerId, countersPlacedOnEntry);
        }
        if (permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, permanent);
        }
        // "Whenever a -1/-1 counter is put on a creature" (Flourishing Defenses) also sees a creature
        // that enters with -1/-1 counters (e.g. Leech Bonder, or persist) — CR ruling.
        permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                gameData, permanent, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE));
        permanentCounterSupport.firePlusOnePlusOneCountersPutOnAnotherNonHydraCreatureTriggers(
                gameData, permanent, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE),
                controllerId, simultaneouslyEntered);
        for (Map.Entry<CounterType, Integer> counter : permanent.getCounters().entrySet()) {
            if (counter.getKey() == CounterType.PLUS_ONE_PLUS_ONE
                    || counter.getKey() == CounterType.MINUS_ONE_MINUS_ONE) {
                continue;
            }
            int added = counter.getValue() - countersBeforeEntry.getOrDefault(counter.getKey(), 0);
            permanentCounterSupport.fireCounterPutOnControlledCreatureTriggers(gameData, permanent, added);
        }
        gameData.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, k -> new ArrayList<>())
                .add(permanent.getCard());
        // Delayed "sacrifice this token at the beginning of the next end step" (Choreographed Sparks).
        if (permanent.getCard().isSacrificeAtEndStep()) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }
        // "As this enters, you may reveal a [subtype] card from your hand; if you don't, it enters
        // tapped." Must run after the permanent is on the battlefield so we can reference/tap it.
        applyRevealSubtypeOrEntersTapped(gameData, controllerId, permanent, conditionalRevealEffect);
        applyMayPayLifeOrEntersTapped(gameData, controllerId, permanent);
        applyUnleash(gameData, controllerId, permanent);
        applyRiot(gameData, controllerId, permanent, simultaneouslyEntered);
        if (simultaneouslyEntered.isEmpty()) {
            gameData.activeMysticReflectionsForEntryBatch.clear();
        }
    }

    private void applyMysticReflectionReplacement(GameData gameData, Permanent permanent,
                                                   List<Permanent> simultaneouslyEntered) {
        if (simultaneouslyEntered.isEmpty()) {
            gameData.activeMysticReflectionsForEntryBatch.clear();
        }
        if (!permanent.getCard().hasType(CardType.CREATURE)
                && !permanent.getCard().hasType(CardType.PLANESWALKER)) {
            return;
        }
        if (gameData.activeMysticReflectionsForEntryBatch.isEmpty()
                && !gameData.pendingMysticReflections.isEmpty()) {
            gameData.activeMysticReflectionsForEntryBatch.addAll(gameData.pendingMysticReflections);
            gameData.pendingMysticReflections.clear();
        }
        for (PendingMysticReflection reflection
                : List.copyOf(gameData.activeMysticReflectionsForEntryBatch)) {
            permanent.setCard(permanent.getCard().createRuntimeCopyWithFace(
                    reflection.lastKnownTargetCard()));
        }
    }

    /**
     * Unleash, as-enters half (CR 702.98a): "You may have this permanent enter with an additional
     * +1/+1 counter on it." The choice needs a player answer, so — like the Lorwyn reveal above —
     * it runs through the pending-may-ability pipeline once the permanent is on the battlefield;
     * {@code UnleashHandler} puts the counter on when the controller accepts. Skipped entirely when
     * the permanent can't have counters (Solemnity), since accepting could do nothing.
     */
    private void applyUnleash(GameData gameData, UUID controllerId, Permanent permanent) {
        boolean unleash = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof UnleashEffect);
        if (!unleash || gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) {
            return;
        }
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                permanent.getCard(),
                controllerId,
                List.of(new UnleashEffect()),
                permanent.getCard().getName() + " — Unleash: have it enter with a +1/+1 counter?"
                        + " (It can't block as long as it has one.)",
                null,
                null,
                permanent.getId()));
        playerInputService.processNextMayAbility(gameData);
    }

    private void applyRiot(GameData gameData, UUID controllerId, Permanent permanent,
                           List<Permanent> simultaneouslyEntered) {
        boolean grantedRiot = gameData.spellsGrantedRiotOnEntry.remove(permanent.getCard().getId());
        boolean creature = gameQueryService.isCreature(gameData, permanent);
        boolean ownRiot = creature && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(RiotEffect.class::isInstance);
        boolean battlefieldRiot = creature && !permanent.getCard().isToken()
                && gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(source -> !source.getId().equals(permanent.getId()))
                .filter(source -> simultaneouslyEntered.stream()
                        .noneMatch(batchMember -> batchMember.getId().equals(source.getId())))
                .flatMap(source -> source.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(ControlledCreaturesHaveRiotEffect.class::isInstance);
        if (!ownRiot && !grantedRiot && !battlefieldRiot) {
            return;
        }
        if (gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
            permanent.getPersistentGrantedKeywords().add(Keyword.HASTE);
            return;
        }
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                permanent.getCard(),
                controllerId,
                List.of(new RiotEffect()),
                permanent.getCard().getName() + " — Riot: have it enter with a +1/+1 counter?"
                        + " (Otherwise it gains haste.)",
                null,
                null,
                permanent.getId()));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * CR 614.12: how a permanent enters is determined against "continuous effects that
     * <em>already exist</em> and would apply to the permanent". Permanents entering in the same
     * simultaneous batch are not yet on the battlefield when that determination happens, so their
     * static and replacement abilities must not apply — a Bramblewood Paragon entering alongside a
     * Warrior does not give that Warrior a +1/+1 counter.
     *
     * <p>The engine places a batch one permanent at a time, so by the time the second member enters
     * the first is physically on the battlefield. This hides every batch-mate from all battlefields
     * for the duration of the replacement-effect window, which makes them invisible to every
     * source scan and every condition evaluated in that window at once, rather than requiring each
     * applier to filter them individually. Batch-mates may sit on different players' battlefields
     * (Warp World has every player entering permanents at the same time), so this searches all of
     * them.
     *
     * <p>Returns per-player snapshots for {@link #restoreHiddenBattlefields}; empty when the batch
     * is empty, which is the case for every single-permanent entry.
     *
     * @see #restoreHiddenBattlefields
     */
    private Map<UUID, List<Permanent>> hideSimultaneouslyEntered(GameData gameData, List<Permanent> simultaneouslyEntered) {
        if (simultaneouslyEntered.isEmpty()) return Map.of();

        Map<UUID, List<Permanent>> snapshots = new HashMap<>();
        gameData.playerBattlefields.forEach((playerId, battlefield) -> {
            List<Permanent> snapshot = List.copyOf(battlefield);
            if (battlefield.removeAll(simultaneouslyEntered)) {
                snapshots.put(playerId, snapshot);
            }
        });
        return snapshots;
    }

    /**
     * Puts the batch-mates hidden by {@link #hideSimultaneouslyEntered} back. Restores each
     * affected battlefield from its snapshot rather than re-adding the removed permanents, because
     * re-adding would append them at the end and reorder the battlefield; order is the CR 613.7
     * equal-timestamp tiebreak, the order triggers reach the stack, and the index the wire protocol
     * addresses permanents by.
     */
    private void restoreHiddenBattlefields(GameData gameData, Map<UUID, List<Permanent>> snapshots) {
        snapshots.forEach((playerId, snapshot) -> {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            battlefield.clear();
            battlefield.addAll(snapshot);
        });
    }

    /**
     * Gather Specimens control-changing replacement effect (CR 614.1): "If a creature would enter the
     * battlefield under an opponent's control this turn, it enters under your control instead." Returns
     * the effective controller for the entering permanent — the gatherer when a creature would enter
     * under one of their opponents' control, otherwise the intended controller unchanged. Idempotent:
     * once a permanent is already assigned to the gatherer, they are not their own opponent.
     */
    public UUID resolveEnteringController(GameData gameData, UUID controllerId, Permanent permanent) {
        if (permanent.getCard().isToken()) {
            return gameQueryService.resolveTokenCreationController(
                    gameData, controllerId, permanent.getCard().hasType(CardType.CREATURE));
        }
        if (gameData.playersGatheringSpecimensThisTurn.isEmpty()
                || !permanent.getCard().hasType(CardType.CREATURE)) {
            return controllerId;
        }
        for (UUID gatherer : gameData.orderedPlayerIds) {
            if (!gatherer.equals(controllerId) && gameData.playersGatheringSpecimensThisTurn.contains(gatherer)) {
                return gatherer;
            }
        }
        return controllerId;
    }

    /**
     * Hallowed Moonlight replacement effect (CR 614.1): "Until end of turn, if a creature would
     * enter and it wasn't cast, exile it instead." Returns {@code true} when the entering permanent
     * was replaced and must not be placed, in which case it never enters and no enters-the-battlefield
     * trigger fires.
     *
     * <p>An entering token is exiled too, but a token outside the battlefield ceases to exist
     * (CR 111.7), so it is simply dropped rather than added to the exile zone. Mistcaller's narrower
     * "nontoken creature" wording is tracked separately and leaves entering tokens alone.
     */
    private boolean applyExileUncastEnteringCreature(GameData gameData, UUID controllerId, Permanent permanent) {
        if (permanent.isCast() || !permanent.getCard().hasType(CardType.CREATURE)) {
            return false;
        }
        Card card = permanent.getCard();
        boolean applies = !gameData.playersExilingUncastEnteringCreaturesThisTurn.isEmpty()
                || (!card.isToken() && !gameData.playersExilingUncastEnteringNontokenCreaturesThisTurn.isEmpty())
                || UncastEnteringCreatureExileSupport.hasActiveStaticReplacement(gameData, card);
        if (!applies) {
            return false;
        }
        if (!card.isToken()) {
            UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : controllerId;
            gameData.addToExile(ownerId, card);
        }
        gameLogService.append(gameData, GameLog.cardThen(card, " is exiled instead of entering the battlefield."));
        log.info("Game {} - {} exiled instead of entering (it wasn't cast)", gameData.id, card.getName());
        return true;
    }

    /**
     * CR 613.7: a text change made to a spell (e.g. Glamerdye targeting a permanent spell) carries
     * onto the permanent that spell resolves into. The replacements were recorded keyed by the
     * spell's card id; the entering permanent shares that card id, so move them onto it.
     */
    private void carrySpellTextReplacements(GameData gameData, Permanent permanent) {
        List<TextReplacement> replacements = gameData.spellTextReplacements.remove(permanent.getCard().getId());
        if (replacements != null) {
            permanent.getTextReplacements().addAll(replacements);
        }
    }

    /**
     * CR 400.7a: a color set on a permanent spell (e.g. Purelace targeting a creature spell "becomes
     * white", or Ersatz Gnomes' "becomes colorless") carries onto the permanent that spell resolves
     * into. The override was recorded keyed by the spell's card id — an empty set meaning colorless;
     * the entering permanent shares that card id, so seed its colors and float an indefinite layer-5
     * color setter (mirroring {@code SetTargetColorEffectHandler}'s permanent path).
     */
    private void carrySpellColorOverride(GameData gameData, UUID controllerId, Permanent permanent) {
        UUID cardId = permanent.getCard().getId();
        Set<CardColor> colors = gameData.spellColorOverrides.remove(cardId);
        Set<CardColor> temporaryColors = gameData.spellColorOverridesUntilEndOfTurn.remove(cardId);
        if (colors == null && temporaryColors == null) {
            return;
        }
        if (colors != null) {
            CardColor color = colors.stream().findFirst().orElse(null);
            permanent.getTransientColors().clear();
            permanent.getTransientColors().addAll(colors);
            permanent.setColorOverridden(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    permanent.getCard().getName(), null, controllerId, new SetTargetColorEffect(color),
                    permanent.getId(), null, null, EffectDuration.PERMANENT, 0));
        }
        if (temporaryColors != null && !temporaryColors.isEmpty()) {
            CardColor color = temporaryColors.iterator().next();
            permanent.getTransientColors().clear();
            permanent.getTransientColors().addAll(temporaryColors);
            permanent.setColorOverridden(true);
            gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                    permanent.getCard().getName(), null, controllerId,
                    new com.github.laxika.magicalvibes.model.effect.GrantColorUntilEndOfTurnEffect(color),
                    permanent.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));
        }
    }

    /**
     * Lorwyn dual-land replacement effect (e.g. Ancient Amphitheater): if the controller can't
     * reveal a card of the required subtype, the permanent enters tapped; otherwise the controller
     * is prompted with a "you may reveal" choice (declining taps the permanent). The prompt reuses
     * the pending-may-ability pipeline; the answer is handled in
     * {@code MayAbilityHandlerService.handleMayAbilityChosen}.
     */
    private RevealSubtypeOrEntersTappedEffect findActiveConditionalRevealEffect(
            GameData gameData, UUID controllerId, Permanent permanent) {
        ConditionContext ctx = ConditionContext.forPermanent(permanent, controllerId);
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof ConditionalReplacementEffect conditional
                        && conditional.upgradedEffect() instanceof RevealSubtypeOrEntersTappedEffect
                        && conditionEvaluationService.isMet(gameData, conditional.condition(), ctx))
                .map(e -> (ConditionalReplacementEffect) e)
                .map(conditional -> (RevealSubtypeOrEntersTappedEffect) conditional.upgradedEffect())
                .findFirst().orElse(null);
    }

    private void applyRevealSubtypeOrEntersTapped(GameData gameData, UUID controllerId, Permanent permanent,
                                                  RevealSubtypeOrEntersTappedEffect activeConditionalEffect) {
        RevealSubtypeOrEntersTappedEffect effect = null;
        for (CardEffect staticEffect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (staticEffect instanceof RevealSubtypeOrEntersTappedEffect revealEffect) {
                effect = revealEffect;
                break;
            }
            if (staticEffect instanceof ConditionalReplacementEffect conditional
                    && conditional.upgradedEffect() instanceof RevealSubtypeOrEntersTappedEffect revealEffect
                    && revealEffect == activeConditionalEffect) {
                    effect = revealEffect;
                    break;
            }
        }
        if (effect == null) {
            return;
        }
        RevealSubtypeOrEntersTappedEffect activeEffect = effect;
        String subtypeDescription = activeEffect.subtypes().stream()
                .map(CardSubtype::getDisplayName)
                .collect(Collectors.joining(" or "));
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean canReveal = hand != null && hand.stream()
                .anyMatch(c -> c.getSubtypes().stream().anyMatch(activeEffect.subtypes()::contains));
        if (!canReveal) {
            permanent.tap();
            log.info("Game {} - {} enters tapped (no {} card to reveal)",
                    gameData.id, permanent.getCard().getName(), subtypeDescription);
            return;
        }
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                permanent.getCard(),
                controllerId,
                List.of(activeEffect),
                permanent.getCard().getName() + " — Reveal a " + subtypeDescription
                        + " card from your hand? (If you don't, it enters tapped.)",
                null,
                null,
                permanent.getId()));
        playerInputService.processNextMayAbility(gameData);
    }

    private void applyMayPayLifeOrEntersTapped(GameData gameData, UUID controllerId, Permanent permanent) {
        MayPayLifeOrEntersTappedEffect effect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof MayPayLifeOrEntersTappedEffect)
                .map(MayPayLifeOrEntersTappedEffect.class::cast)
                .findFirst().orElse(null);
        if (effect == null) {
            return;
        }

        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, controllerId)
                && gameData.getLife(controllerId) >= effect.lifeCost();
        if (!canPay) {
            permanent.tap();
            log.info("Game {} - {} enters tapped (controller cannot pay {} life)", gameData.id,
                    permanent.getCard().getName(), effect.lifeCost());
            return;
        }

        gameData.pendingMayAbilities.add(new PendingMayAbility(
                permanent.getCard(),
                controllerId,
                List.of(effect),
                permanent.getCard().getName() + " — Pay " + effect.lifeCost()
                        + " life? (If you don't, it enters tapped.)",
                null,
                null,
                permanent.getId()));
        playerInputService.processNextMayAbility(gameData);
    }

    /**
     * CR 614.1c — "Creatures you control enter as a copy of this creature."
     * If the entering permanent is a creature and the controller has a permanent with
     * {@link CreaturesEnterAsCopyOfSourceEffect}, the entering creature becomes a copy
     * of that source permanent. This is mandatory (not a "may" ability).
     *
     * <p>With {@code copyEnchantedCreature} the copied permanent is the creature the source Aura is
     * attached to instead (Infinite Reflection); an unattached source does nothing. {@code nontokenOnly}
     * skips entering tokens.
     */
    private void applyCreaturesEnterAsCopyReplacementEffect(GameData gameData, UUID controllerId, Permanent entering) {
        if (!entering.getCard().hasType(CardType.CREATURE)) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            CreaturesEnterAsCopyOfSourceEffect effect = source.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(e -> e instanceof CreaturesEnterAsCopyOfSourceEffect)
                    .map(e -> (CreaturesEnterAsCopyOfSourceEffect) e)
                    .findFirst().orElse(null);
            if (effect != null) {
                if (effect.nontokenOnly() && entering.getCard().isToken()) {
                    continue;
                }
                Permanent copied = source;
                if (effect.copyEnchantedCreature()) {
                    copied = source.getAttachedTo() == null ? null
                            : gameQueryService.findPermanentById(gameData, source.getAttachedTo());
                    if (copied == null || copied.getId().equals(entering.getId())) {
                        continue;
                    }
                }
                permanentCopierService.applyCloneCopy(entering, copied, null, null);
                // Reset any counters that were pre-set by the original card's "enters with"
                // replacement effects — the creature now enters as Essence, which has no such
                // effects, so those counters should not apply.
                entering.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
                entering.setCounterCount(CounterType.CHARGE, 0);
                entering.setCounterCount(CounterType.WISH, 0);
                return;
            }
        }
    }

    public Set<CardType> snapshotEnterTappedTypes(GameData gameData) {
        Set<CardType> enterTappedTypes = EnumSet.noneOf(CardType.class);

        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof EnterPermanentsOfTypesTappedEffect enterTapped)) {
                    continue;
                }
                if (enterTapped.opponentsOnly()) {
                    continue;
                }
                enterTappedTypes.addAll(enterTapped.cardTypes());
            }
        });
        return enterTappedTypes;
    }

    private void applyEnterTappedEffects(Permanent enteringPermanent, Set<CardType> enterTappedTypes) {
        if (enterTappedTypes == null || enterTappedTypes.isEmpty()) {
            return;
        }
        if (matchesAnyType(enteringPermanent.getCard(), enterTappedTypes)) {
            enteringPermanent.tap();
        }
    }

    private void applyAllPermanentsEnterTapped(GameData gameData, Permanent enteringPermanent) {
        if (gameData.allPermanentsEnterTappedThisTurn) {
            enteringPermanent.tap();
        }
    }

    private void applyOpponentOnlyEnterTappedEffects(GameData gameData, UUID enteringControllerId, Permanent enteringPermanent) {
        gameData.forEachBattlefield((sourcePlayerId, battlefield) -> {
            if (sourcePlayerId.equals(enteringControllerId)) return;

            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof EnterPermanentsOfTypesTappedEffect enterTapped)) {
                        continue;
                    }
                    if (!enterTapped.opponentsOnly()) {
                        continue;
                    }
                    if (matchesEnterTappedEffect(gameData, enteringPermanent, enterTapped)) {
                        enteringPermanent.tap();
                    }
                }
            }
        });
    }

    private boolean matchesEnterTappedEffect(GameData gameData, Permanent enteringPermanent,
                                             EnterPermanentsOfTypesTappedEffect enterTapped) {
        if (enterTapped.filter() != null) {
            return predicateEvaluationService.matchesPermanentPredicate(gameData, enteringPermanent, enterTapped.filter());
        }
        return matchesAnyType(enteringPermanent.getCard(), enterTapped.cardTypes());
    }

    /**
     * "Each creature without mana value of the chosen quality enters tapped" (Ashling's Prerogative).
     * For each permanent carrying {@link CreaturesOfUnchosenParityEnterTappedEffect} with a chosen
     * parity, an entering creature whose mana value does not match that parity enters tapped. Applies
     * across all battlefields; while the source's parity is unchosen (null) it does nothing.
     */
    private void applyUnchosenParityEnterTapped(GameData gameData, Permanent enteringPermanent) {
        if (!enteringPermanent.getCard().hasType(CardType.CREATURE)) {
            return;
        }
        int manaValue = enteringPermanent.getCard().getManaValue();
        gameData.forEachPermanent((playerId, source) -> {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof CreaturesOfUnchosenParityEnterTappedEffect)) {
                    continue;
                }
                ManaValueParity chosen = source.getChosenManaValueParity();
                if (chosen != null && !chosen.matches(manaValue)) {
                    enteringPermanent.tap();
                }
            }
        });
    }

    /**
     * "If this land would enter, instead sacrifice each other permanent named [this] you control,
     * then put this land onto the battlefield." (Sheltered Valley.)
     *
     * <p>Runs before the entering permanent is placed, so it can never sacrifice itself. The
     * entering permanent always enters; only the older copies leave.
     */
    private void applySacrificeOtherPermanentsWithSameName(GameData gameData, UUID controllerId, Permanent permanent) {
        boolean present = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(SacrificeOtherPermanentsWithSameNameOnEnterEffect.class::isInstance);
        if (!present) {
            return;
        }
        String name = permanent.getCard().getName();
        List<Permanent> doomed = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(p -> name.equals(p.getCard().getName()))
                .toList();
        for (Permanent other : doomed) {
            if (permanentRemovalService.removePermanentToGraveyard(gameData, other)) {
                triggerCollectionService.checkAllyPermanentSacrificedTriggers(gameData, controllerId, other.getCard());
                gameLogService.append(gameData, GameLog.cardThen(other.getCard(), " is sacrificed."));
            }
        }
        if (!doomed.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    /**
     * "If this permanent would enter, sacrifice a [permanent] instead. If you do, put it onto the
     * battlefield. If you don't, put it into its owner's graveyard." (Balduvian Trading Post.)
     *
     * <p>Returns {@code false} when the permanent must not be placed right now — either it went to
     * the graveyard (no legal sacrifice available) or the controller is being prompted to choose
     * one, in which case entry resumes in {@link #completeSacrificePermanentToEnter} or
     * {@link #completeSacrificePermanentsToEnter}.
     */
    private boolean applyEntryCostReplacement(GameData gameData, UUID controllerId, Permanent permanent) {
        if (permanent.isEntryCostPaid()) {
            return true;
        }
        EntryCostReplacementEffect effect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(EntryCostReplacementEffect.class::isInstance)
                .map(EntryCostReplacementEffect.class::cast)
                .findFirst().orElse(null);
        if (effect == null) {
            return true;
        }
        if (effect.kind() == EntryCostReplacementEffect.Kind.DISCARD_CARD) {
            return applyDiscardCardToEnter(gameData, controllerId, permanent, effect);
        }

        List<UUID> sacrificeable = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, effect.permanentFilter()))
                .map(Permanent::getId)
                .toList();
        if (sacrificeable.size() < effect.count()) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, effect);
            return false;
        }
        if (effect.count() > 1) {
            playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                    new ArrayList<>(sacrificeable), effect.count(),
                    new MultiPermanentChoiceContext.SacrificePermanentsToEnter(
                            controllerId, permanent, effect.count()),
                    permanent.getCard().getName() + " — Sacrifice " + effect.description()
                            + " to have " + permanent.getCard().getName()
                            + " enter (choose none to decline).");
            return false;
        }
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificePermanentToEnter(controllerId, permanent));
        playerInputService.beginAnyTargetChoice(gameData, controllerId, sacrificeable, List.of(controllerId),
                permanent.getCard().getName() + " — Sacrifice " + effect.description()
                        + "? (Choose yourself to decline; " + permanent.getCard().getName()
                        + " is then put into its owner's graveyard.)");
        return false;
    }

    private boolean applyRequiredGraveyardExileReplacement(GameData gameData, UUID controllerId,
                                                           Permanent permanent, int xValue) {
        ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect effect = permanent.getCard()
                .getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect.class::isInstance)
                .map(ExileXCreatureCardsFromGraveyardOnEnterWithCountersEffect.class::cast)
                .findFirst().orElse(null);
        if (effect == null || xValue <= 0) {
            return true;
        }

        long creatureCardCount = gameData.playerGraveyards.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .count();
        if (creatureCardCount >= xValue) {
            return true;
        }

        putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, null);
        return false;
    }

    private boolean applyDiscardCardToEnter(GameData gameData, UUID controllerId, Permanent permanent,
                                             EntryCostReplacementEffect effect) {
        List<Card> hand = gameData.playerHands.getOrDefault(controllerId, List.of());
        List<Integer> discardable = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            if (predicateEvaluationService.matchesCardPredicate(
                    hand.get(i), effect.cardFilter(), permanent.getCard().getId())) {
                discardable.add(i);
            }
        }
        if (discardable.isEmpty()) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, effect);
            return false;
        }

        playerInputService.beginDiscardChoice(gameData, controllerId, discardable,
                permanent.getCard().getName() + " — Discard " + effect.description()
                        + " to have it enter (choose no card to decline).", 1,
                DiscardFollowUp.enteringPermanent(permanent, controllerId), null, true);
        return false;
    }

    /**
     * Resumes a {@link PermanentChoiceContext.SacrificePermanentToEnter} choice: {@code null} means
     * the controller declined (the card goes to its owner's graveyard), otherwise the chosen
     * permanent has already been sacrificed by the caller and the parked permanent now enters.
     */
    public void completeSacrificePermanentToEnter(GameData gameData, UUID controllerId, Permanent permanent, boolean sacrificed) {
        if (!sacrificed) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, null);
            return;
        }
        permanent.setEntryCostPaid(true);
        place(gameData, defaultRequest(gameData, controllerId, permanent));
    }

    public void completeSacrificePermanentsToEnter(GameData gameData, UUID controllerId, Permanent permanent,
                                                   boolean sacrificed) {
        if (!sacrificed) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, null);
            return;
        }
        permanent.setEntryCostPaid(true);
        place(gameData, defaultRequest(gameData, controllerId, permanent));
    }

    public void completeDiscardCardToEnter(GameData gameData, UUID controllerId, Permanent permanent,
                                           boolean discarded) {
        if (!discarded) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, null);
            return;
        }
        permanent.setEntryCostPaid(true);
        place(gameData, defaultRequest(gameData, controllerId, permanent));
    }

    private BattlefieldEntryRequest defaultRequest(
            GameData gameData, UUID controllerId, Permanent permanent) {
        return new BattlefieldEntryRequest(controllerId, permanent, snapshotEnterTappedTypes(gameData),
                List.of(), 0, false, List.of());
    }

    private void putEnteringPermanentIntoGraveyard(GameData gameData, UUID controllerId, Permanent permanent,
                                                    EntryCostReplacementEffect effect) {
        Card card = permanent.getCard();
        UUID ownerId = card.getOwnerId() != null ? card.getOwnerId() : controllerId;
        graveyardService.addCardToGraveyard(gameData, ownerId, card);
        gameLogService.append(gameData, GameLog.cardThen(card, " is put into its owner's graveyard instead of entering."));
        log.info("Game {} - {} put into graveyard instead of entering ({})", gameData.id, card.getName(),
                effect == null ? "declined" : "no " + effect.description());
    }

    private void applySelfEnterTapped(Permanent enteringPermanent) {
        boolean entersTapped = enteringPermanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof EntersTappedEffect);
        if (entersTapped) {
            enteringPermanent.tap();
        }
    }

    /**
     * "This permanent enters tapped unless …" replacement effects (check lands, fast lands,
     * slow lands). Each is a {@link ConditionalReplacementEffect} wrapping an
     * {@link EntersTappedEffect}: the wrapped condition is the <em>negated</em> unless-clause
     * (true when the permanent should enter tapped). The condition is evaluated relative to the
     * entering permanent's controller; since the permanent is not yet on the battlefield (added
     * after this method), "other lands" / "matching permanents" counts naturally exclude it.
     */
    private void applyConditionalEnterTapped(GameData gameData, UUID controllerId, Permanent enteringPermanent) {
        for (CardEffect effect : enteringPermanent.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof ConditionalReplacementEffect conditional
                    && conditional.upgradedEffect() instanceof EntersTappedEffect) {
                ConditionContext ctx = ConditionContext.forPermanent(enteringPermanent, controllerId);
                if (conditionEvaluationService.isMet(gameData, conditional.condition(), ctx)) {
                    enteringPermanent.tap();
                }
            }
        }
    }

    private boolean matchesAnyType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) {
            return true;
        }
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replacement effect (MTG Rule 614.1c): "This permanent enters the battlefield with
     * [count] [type] counters on it." Evaluates each {@link EnterWithCountersEffect} on the
     * entering permanent — either bare or wrapped in a {@link ConditionalEffect} ("if kicked",
     * "Raid —") — and puts the counters on the permanent before it is added to the battlefield,
     * so ETB triggers and static/CDA evaluation already see them (CR 614.12).
     *
     * <p>The permanent is not on the battlefield yet, so battlefield-counting amounts naturally
     * exclude it ("for each <em>other</em> [subtype] you control", e.g. Unbreathing Horde).</p>
     */
    private void applyEnterWithCounters(GameData gameData, UUID controllerId, Permanent permanent,
                                        int xValue, boolean kicked, List<String> repeatedAdditionalCosts) {
        Card card = permanent.getCard();
        // Solemnity and Tatterkite/Melira's Keepers-style locks also replace "enters with N counters".
        if (gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) return;

        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            EnterWithCountersEffect enterWith;
            if (effect instanceof EnterWithCountersEffect direct) {
                enterWith = direct;
            } else if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof EnterWithCountersEffect wrapped) {
                ConditionContext conditionContext = new ConditionContext(controllerId, null, permanent,
                        card, kicked, false, permanent.isProwl(), false, false, false,
                        permanent.getCastFromZone(), xValue, null, null, false,
                        false, false, null, null, null, repeatedAdditionalCosts);
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(), conditionContext)) {
                    continue;
                }
                enterWith = wrapped;
            } else {
                continue;
            }

            if (permanent.getChosenSubtype() == null && isChosenSubtypeDependent(enterWith)) continue;
            applyEnterWithCountersEffect(gameData, controllerId, permanent, enterWith, xValue,
                    repeatedAdditionalCosts, card);
        }

        applyGrantedBloodthirst(gameData, controllerId, permanent);
        applySpellAdditionalEnterCounters(gameData, controllerId, permanent);
        applySpellGrantedHaste(gameData, permanent);
    }

    /**
     * Applies entry counters whose amount depends on the subtype chosen for the entering permanent.
     * The permanent is already on the battlefield when the choice is answered, but this runs before
     * its ETB effects are collected, preserving the card's entry-time behavior.
     */
    public void applyDeferredEnterWithCounters(GameData gameData, UUID controllerId, Permanent permanent) {
        if (permanent.getChosenSubtype() == null
                || gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) return;

        int countersBefore = permanent.getCounters().values().stream().mapToInt(Integer::intValue).sum();
        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof EnterWithCountersEffect enterWith && isChosenSubtypeDependent(enterWith)) {
                applyEnterWithCountersEffect(gameData, controllerId, permanent, enterWith, 0,
                        List.of(), permanent.getCard());
            }
        }
        int countersPlaced = permanent.getCounters().values().stream().mapToInt(Integer::intValue).sum() - countersBefore;
        if (countersPlaced > 0) {
            triggerCollectionService.checkYouPutCountersTriggers(gameData, controllerId, countersPlaced);
        }
    }

    private void applyEnterWithCountersEffect(GameData gameData, UUID controllerId, Permanent permanent,
                                              EnterWithCountersEffect enterWith, int xValue,
                                              List<String> repeatedAdditionalCosts, Card sourceCard) {
        int count = amountEvaluationService.evaluate(gameData, enterWith.count(),
                new AmountContext(controllerId, permanent, null, xValue, 0, false, null,
                        repeatedAdditionalCosts == null ? List.of() : repeatedAdditionalCosts, sourceCard));
        applyEntryCounters(gameData, controllerId, permanent, enterWith.type(), count);
    }

    private void applyDiscardEntryCounters(GameData gameData, UUID controllerId, Permanent permanent,
                                           EnterBattlefieldOnDiscardEffect discardReplacement) {
        if (discardReplacement == null) {
            return;
        }
        applyEntryCounters(gameData, controllerId, permanent,
                discardReplacement.counterType(), discardReplacement.counterCount());
    }

    private void applyEntryCounters(GameData gameData, UUID controllerId, Permanent permanent,
                                    CounterType counterType, int count) {
        if (counterType == null || count <= 0) {
            return;
        }
        if (counterType == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)) {
            count = 0;
        } else if (counterType == CounterType.PLUS_ONE_PLUS_ONE
                && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent, controllerId)) {
            count = 0;
        }
        count = gameQueryService.replaceCounters(gameData, permanent, controllerId, counterType, count);
        if (count > 0) {
            permanent.setCounterCount(counterType, permanent.getCounterCount(counterType) + count);
            log.info("Game {} - {} enters with {} {} counter(s)",
                    gameData.id, permanent.getCard().getName(), count, counterType);
        }
    }

    private boolean isChosenSubtypeDependent(EnterWithCountersEffect enterWith) {
        return enterWith.count() instanceof PermanentCount count
                && containsChosenSubtypePredicate(count.filter());
    }

    private boolean containsChosenSubtypePredicate(PermanentPredicate predicate) {
        return switch (predicate) {
            case PermanentHasSourceChosenSubtypePredicate ignored -> true;
            case PermanentAllOfPredicate all -> all.predicates().stream().anyMatch(this::containsChosenSubtypePredicate);
            case PermanentAnyOfPredicate any -> any.predicates().stream().anyMatch(this::containsChosenSubtypePredicate);
            case PermanentNotPredicate not -> containsChosenSubtypePredicate(not.predicate());
            default -> false;
        };
    }

    /**
     * Haste granted by the mana that paid for the creature spell (Generator Servant). The grant is
     * keyed by card id and consumed here, so it applies only to the permanent that spell becomes; it
     * wears off with the permanent's other until-end-of-turn keyword grants.
     */
    private void applySpellGrantedHaste(GameData gameData, Permanent permanent) {
        if (gameData.spellsGrantedHasteOnEntry.remove(permanent.getCard().getId())) {
            permanent.getGrantedKeywords().add(Keyword.HASTE);
            log.info("Game {} - {} enters with haste (paid for with haste-granting mana)",
                    gameData.id, permanent.getCard().getName());
        }
    }

    /**
     * Extra +1/+1 counters granted to the creature spell before it was cast (Savage Summoning's
     * "That creature enters with an additional +1/+1 counter on it"). The grant is keyed by card id
     * and consumed here, so it applies only to the permanent that spell becomes.
     */
    private void applySpellAdditionalEnterCounters(
            GameData gameData, UUID controllerId, Permanent permanent) {
        Integer granted = gameData.spellAdditionalEnterCounters.remove(permanent.getCard().getId());
        if (granted == null || granted <= 0) return;

        granted = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, controllerId, granted);
        if (granted <= 0) return;
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + granted);
        log.info("Game {} - {} enters with {} additional +1/+1 counter(s) granted to its spell",
                gameData.id, permanent.getCard().getName(), granted);
    }

    /**
     * Bloodthirst granted to the spell while it was on the stack (Bloodlord of Vaasgoth). Bloodthirst
     * is a static ability — "If an opponent was dealt damage this turn, this permanent enters with N
     * +1/+1 counters on it" (CR 702.54a) — so the grant resolves here as an as-enters replacement,
     * separately from any bloodthirst printed on the card (CR 702.54c).
     */
    private void applyGrantedBloodthirst(GameData gameData, UUID controllerId, Permanent permanent) {
        int granted = permanent.getGrantedBloodthirst();
        if (granted <= 0) return;

        ConditionContext conditionContext = new ConditionContext(controllerId, null, permanent,
                permanent.getCard(), false, false, false, false, permanent.getCastFromZone(), 0, null, null, false);
        if (!conditionEvaluationService.isMet(gameData, new OpponentDealtDamageThisTurn(1), conditionContext)) {
            return;
        }

        granted = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, controllerId, granted);
        if (granted <= 0) return;
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + granted);
        log.info("Game {} - {} enters with {} +1/+1 counter(s) from granted bloodthirst",
                gameData.id, permanent.getCard().getName(), granted);
    }

    /**
     * CR 614.12 lookahead: determines whether a permanent <em>about to enter the battlefield</em>
     * would have the given subtype once all static effects are applied. This considers:
     * <ol>
     *   <li>The permanent's natural subtypes (from its card).</li>
     *   <li>Transient and granted subtypes already on the permanent.</li>
     *   <li>The Changeling keyword (natural or granted by static effects).</li>
     *   <li>Static effects from permanents already on the battlefield (e.g., Xenograft,
     *       Conspiracy, lord effects that grant subtypes).</li>
     * </ol>
     *
     * <p>Per CR 614.12, when multiple permanents enter simultaneously they <em>cannot</em>
     * see each other. The {@code simultaneouslyEntered} parameter lists permanents that were
     * already placed on the battlefield as part of the same simultaneous batch and must be
     * <em>excluded</em> from the lookahead.
     *
     * <p>Convenience wrapper for a single-subtype question. The work happens in
     * {@link #resolveEnteringSubtypes}, which answers for every subtype at once; a caller that
     * tests several subtypes against the same entering permanent should resolve once and reuse
     * the result rather than calling this in a loop.
     *
     * @param gameData               current game state
     * @param entering               the permanent about to enter the battlefield
     * @param controllerId           the controller under whose control it will enter
     * @param simultaneouslyEntered  permanents already on the battlefield from this simultaneous
     *                               batch that should be excluded from the lookahead; may be empty
     * @param subtype                the subtype to check for
     * @return {@code true} if the permanent would have the subtype on the battlefield
     */
    public boolean permanentWouldHaveSubtype(GameData gameData, Permanent entering, UUID controllerId,
                                              List<Permanent> simultaneouslyEntered, CardSubtype subtype) {
        return hasSubtype(resolveEnteringSubtypes(gameData, entering, controllerId, simultaneouslyEntered), subtype);
    }

    /**
     * Every subtype an entering permanent would have once static effects are applied, as resolved
     * by a single {@link #resolveEnteringSubtypes} pass. {@code changeling} stands for "every
     * creature type" (CR 702.73a) instead of being expanded into {@code subtypes}, because that
     * full set is not enumerable here; {@link #hasSubtype} applies it.
     */
    private record EnteringSubtypes(Set<CardSubtype> subtypes, boolean changeling) {}

    private boolean hasSubtype(EnteringSubtypes resolved, CardSubtype subtype) {
        if (resolved.subtypes().contains(subtype)) return true;
        return resolved.changeling() && gameQueryService.isCreatureSubtype(subtype);
    }

    /**
     * Runs the CR 614.12 lookahead once, collecting the entering permanent's natural, transient and
     * already-granted subtypes plus everything the battlefield's static effects would grant it.
     *
     * <p><strong>This mutates the controller's live battlefield list.</strong> It temporarily
     * splices the entering permanent in (and removes any {@code simultaneouslyEntered} permanents),
     * runs {@link GameQueryService#computeStaticBonus}, then restores the list from a snapshot in a
     * {@code finally} block. Two consequences for callers: it must run on the game thread like
     * every other entry-time step, and <em>no caller may be iterating a battlefield list when it
     * calls this</em> — doing so throws {@link java.util.ConcurrentModificationException}. Both
     * counter callers therefore gather their matching effects first and resolve afterwards.
     * That splice is also why this lives here rather than on the read-only
     * {@link GameQueryService}.
     *
     * <p>Removing {@code simultaneouslyEntered} here is redundant when called from the entry
     * funnel, which has already hidden the whole batch from every battlefield
     * ({@link #hideSimultaneouslyEntered}). It is kept so the public
     * {@link #permanentWouldHaveSubtype} is correct when called standalone, and it is a harmless
     * no-op otherwise — both layers restore from their own snapshot, so they nest safely.
     *
     * <p>The restore replays a snapshot rather than inverting each mutation, because
     * {@code addAll} is not the inverse of {@code removeAll}: it would re-append the excluded
     * permanents at the end and permanently reorder the battlefield. Order is load-bearing —
     * it is the CR 613.7 equal-timestamp tiebreak in {@code LayerSystemService}, the order
     * triggers reach the stack, and the index the wire protocol addresses permanents by.
     */
    private EnteringSubtypes resolveEnteringSubtypes(GameData gameData, Permanent entering, UUID controllerId,
                                                      List<Permanent> simultaneouslyEntered) {
        Set<CardSubtype> subtypes = EnumSet.noneOf(CardSubtype.class);
        subtypes.addAll(entering.getCard().getSubtypes());
        subtypes.addAll(entering.getTransientSubtypes());
        subtypes.addAll(entering.getGrantedSubtypes());
        boolean changeling = entering.getCard().getKeywords().contains(Keyword.CHANGELING);

        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        List<Permanent> snapshot = List.copyOf(bf);
        bf.add(entering);
        bf.removeAll(simultaneouslyEntered);
        try {
            GameQueryService.StaticBonus bonus = gameQueryService.computeStaticBonus(gameData, entering);
            subtypes.addAll(bonus.grantedSubtypes());
            // A static effect might grant Changeling
            changeling |= bonus.keywords().contains(Keyword.CHANGELING);
        } finally {
            bf.clear();
            bf.addAll(snapshot);
        }
        return new EnteringSubtypes(subtypes, changeling);
    }

    /**
     * Replacement effect (MTG Rule 614.1c): checks the controller's graveyard for cards with
     * {@link GraveyardEnterWithAdditionalCountersEffect} and adds +1/+1 counters to matching
     * creatures as they enter the battlefield. Uses CR 614.12 lookahead via
     * {@link #permanentWouldHaveSubtype} to determine subtypes.
     *
     * @param simultaneouslyEntered permanents to exclude from lookahead (see CR 614.12)
     */
    private void applyGraveyardEnterWithAdditionalCounters(GameData gameData, UUID controllerId,
                                                            Permanent permanent, List<Permanent> simultaneouslyEntered) {
        if (!permanent.getCard().hasType(CardType.CREATURE)) return;

        if (gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) return;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) return;

        List<GraveyardEnterWithAdditionalCountersEffect> effects = graveyard.stream()
                .flatMap(card -> card.getEffects(EffectSlot.STATIC).stream())
                .filter(GraveyardEnterWithAdditionalCountersEffect.class::isInstance)
                .map(GraveyardEnterWithAdditionalCountersEffect.class::cast)
                .toList();
        if (effects.isEmpty()) return;

        EnteringSubtypes resolved = resolveEnteringSubtypes(gameData, permanent, controllerId, simultaneouslyEntered);
        int additionalCounters = effects.stream()
                .filter(effect -> hasSubtype(resolved, effect.subtype()))
                .mapToInt(GraveyardEnterWithAdditionalCountersEffect::count)
                .sum();

        if (additionalCounters > 0) {
            additionalCounters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, permanent, controllerId, additionalCounters);
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from graveyard effect(s)",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }

    /**
     * Replacement effect (MTG Rule 614.1c): checks the controller's battlefield for permanents with
     * {@link ControlledCreaturesEnterWithAdditionalCountersEffect} and adds +1/+1 counters to matching
     * creatures as they enter. "Other" is implicit — the entering permanent is not yet on the
     * battlefield, and a source entering simultaneously does not apply its effect (CR 614.12).
     *
     * @param simultaneouslyEntered permanents to exclude from subtype lookahead (see CR 614.12)
     */
    private void applyControlledCreaturesEnterWithAdditionalCounters(GameData gameData, UUID controllerId,
                                                                     Permanent permanent, List<Permanent> simultaneouslyEntered) {
        if (!permanent.getCard().hasType(CardType.CREATURE)) return;

        if (gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.isEmpty()) return;

        record SourcedCounters(Permanent source, CardSubtype subtype, DynamicAmount count) {}
        List<SourcedCounters> effects = battlefield.stream()
                .flatMap(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                        .filter(ControlledCreaturesEnterWithAdditionalCountersEffect.class::isInstance)
                        .map(ControlledCreaturesEnterWithAdditionalCountersEffect.class::cast)
                        .map(effect -> new SourcedCounters(
                                source,
                                effect.subtype() != null ? effect.subtype() : source.getChosenSubtype(),
                                effect.count())))
                .filter(sourced -> sourced.subtype() != null)
                .toList();
        if (effects.isEmpty()) return;

        EnteringSubtypes resolved = resolveEnteringSubtypes(gameData, permanent, controllerId, simultaneouslyEntered);
        int additionalCounters = effects.stream()
                .filter(sourced -> hasSubtype(resolved, sourced.subtype()))
                .mapToInt(sourced -> amountEvaluationService.evaluate(gameData, sourced.count(),
                        new AmountContext(controllerId, sourced.source(), null, 0, 0)))
                .sum();

        if (additionalCounters > 0) {
            additionalCounters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, permanent, controllerId, additionalCounters);
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from battlefield static effect(s)",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }

    private void applyControlledPermanentEntryReplacements(GameData gameData, UUID controllerId,
                                                           Permanent permanent) {
        if (gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.isEmpty()) return;

        int additionalCounters = 0;
        for (Permanent source : battlefield) {
            FilterContext sourceContext = FilterContext.of(gameData)
                    .withSourceCardId(source.getCard().getId())
                    .withSourceControllerId(controllerId)
                    .withSourcePermanentSnapshot(source)
                    .withSourcePermanentId(source.getId());
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof ControlledPermanentEntryReplacementEffect replacement)) continue;
                if (predicateEvaluationService.matchesPermanentPredicate(
                        permanent, replacement.enteringPermanentPredicate(), sourceContext)) {
                    additionalCounters += Math.max(0, replacement.additionalCounterCount(permanent));
                }
            }
        }

        if (additionalCounters > 0) {
            additionalCounters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, permanent, controllerId, additionalCounters);
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from a mana-value entry effect",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }

    /**
     * Replacement effect (MTG Rule 614.1c) recorded on the game state for the rest of the turn
     * (Zameck Guildmage): each creature entering under {@code controllerId}'s control gets the
     * recorded number of additional +1/+1 counters. Unlike the battlefield-static variant this
     * keeps working after the source leaves, and applies to every creature — not just "other" ones.
     */
    private void applyAdditionalEnterCountersThisTurn(GameData gameData, UUID controllerId, Permanent permanent) {
        if (!permanent.getCard().hasType(CardType.CREATURE)) return;

        if (gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId)) return;

        int additionalCounters = gameData.additionalEnterCountersThisTurn.getOrDefault(controllerId, 0);
        if (additionalCounters <= 0) return;

        additionalCounters = gameQueryService.doublePlusOnePlusOneCounters(
                gameData, permanent, controllerId, additionalCounters);
        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
        log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from a turn-long effect",
                gameData.id, permanent.getCard().getName(), additionalCounters);
    }

    /**
     * Replacement effect (MTG Rule 614.1c) for Master Biomancer: each other creature the source's
     * controller controls enters with additional +1/+1 counters equal to the source's power and with
     * an extra subtype in addition to its other types. "Other" is implicit — the entering permanent
     * is not on the battlefield yet.
     * <p>
     * The subtype grant is applied even when counters can't be placed (Solemnity), since the two
     * halves of the replacement are independent.
     */
    private void applyControlledCreaturesEnterWithSourcePowerCounters(GameData gameData, UUID controllerId,
                                                                      Permanent permanent) {
        if (!permanent.getCard().hasType(CardType.CREATURE)) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.isEmpty()) return;

        boolean noCounters = gameQueryService.cantHaveCountersForController(gameData, permanent, controllerId);
        int additionalCounters = 0;
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof ControlledCreaturesEnterWithSourcePowerCountersEffect e)) continue;
                if (!permanent.getGrantedSubtypes().contains(e.addedSubtype())) {
                    permanent.getGrantedSubtypes().add(e.addedSubtype());
                }
                additionalCounters += Math.max(0, gameQueryService.getEffectivePower(gameData, source));
            }
        }

        if (additionalCounters > 0 && !noCounters) {
            additionalCounters = gameQueryService.doublePlusOnePlusOneCounters(
                    gameData, permanent, controllerId, additionalCounters);
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from a source-power static effect",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }
}
