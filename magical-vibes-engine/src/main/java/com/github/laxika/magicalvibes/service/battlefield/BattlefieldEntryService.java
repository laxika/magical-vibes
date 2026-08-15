package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.effect.ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSourceChosenSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.condition.OpponentDealtDamageThisTurn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePrimalClayFormOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.PayAnyAmountOfLifeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfUnchosenParityEnterTappedEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DevourEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsAsEntersForCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.UnleashEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.SetTargetColorEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithSourcePowerCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.EntryCostReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherPermanentsWithSameNameOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectContext;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
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
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class BattlefieldEntryService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final PermanentCopierService permanentCopierService;
    private final TriggerCollectionService triggerCollectionService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final EtbEffectResolver etbEffectResolver;
    private final AmountEvaluationService amountEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport;
    private com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeAllPermanentsAsEntersEffectHandler sacrificeAllPermanentsAsEntersEffectHandler;
    private final com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService;
    private final PermanentRemovalService permanentRemovalService;

    // @Lazy on triggerCollectionService and permanentCounterSupport breaks the constructor cycle:
    // BattlefieldEntryService → TriggerCollectionService/PermanentCounterSupport →
    // PlayerInputService/queue services → (effect handlers) → BattlefieldEntryService.
    public BattlefieldEntryService(GameQueryService gameQueryService,
                                   GameLogService gameLogService,
                                   PlayerInputService playerInputService,
                                   PermanentCopierService permanentCopierService,
                                   @Lazy TriggerCollectionService triggerCollectionService,
                                   GraveyardTargetingService graveyardTargetingService,
                                   ETBTokenTargetService etbTokenTargetService,
                                   EtbEffectResolver etbEffectResolver,
                                   AmountEvaluationService amountEvaluationService,
                                   ConditionEvaluationService conditionEvaluationService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport,
                                   com.github.laxika.magicalvibes.service.graveyard.GraveyardService graveyardService,
                                   @Lazy PermanentRemovalService permanentRemovalService) {
        this.permanentRemovalService = permanentRemovalService;
        this.graveyardService = graveyardService;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.permanentCopierService = permanentCopierService;
        this.triggerCollectionService = triggerCollectionService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.etbEffectResolver = etbEffectResolver;
        this.amountEvaluationService = amountEvaluationService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.permanentCounterSupport = permanentCounterSupport;
    }

    @Autowired
    void setSacrificeAllPermanentsAsEntersEffectHandler(
            @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.SacrificeAllPermanentsAsEntersEffectHandler handler) {
        this.sacrificeAllPermanentsAsEntersEffectHandler = handler;
    }


    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent) {
        putPermanentOntoBattlefield(gameData, controllerId, permanent, snapshotEnterTappedTypes(gameData), List.of(), 0, false);
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent, Set<CardType> enterTappedTypes) {
        putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes, List.of(), 0, false);
    }

    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                             Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered) {
        putPermanentOntoBattlefield(gameData, controllerId, permanent, enterTappedTypes, simultaneouslyEntered, 0, false);
    }

    /**
     * Entry point for resolving permanent spells: carries the spell's snapshotted cast context
     * (X paid, kicked) so "enters with X … counters" and "if kicked / Raid" as-enters
     * replacement effects can read it.
     */
    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                             int xValue, boolean kicked) {
        putPermanentOntoBattlefield(gameData, controllerId, permanent, snapshotEnterTappedTypes(gameData), List.of(), xValue, kicked);
    }

    /**
     * Core battlefield entry method. All overloads delegate here.
     *
     * @param simultaneouslyEntered permanents already placed on the battlefield as part of the
     *                              same simultaneous batch (e.g. mass reanimation) that must be
     *                              <em>excluded</em> from the CR 614.12 lookahead; may be empty
     * @param xValue                X paid for the spell the permanent resolves from (0 when the
     *                              permanent wasn't cast, e.g. tokens and reanimation)
     * @param kicked                whether the spell was kicked (false when not cast)
     */
    public void putPermanentOntoBattlefield(GameData gameData, UUID controllerId, Permanent permanent,
                                             Set<CardType> enterTappedTypes, List<Permanent> simultaneouslyEntered,
                                             int xValue, boolean kicked) {
        controllerId = resolveEnteringController(gameData, controllerId, permanent);
        int counterCountBeforeEntry = permanent.getCounters().values().stream().mapToInt(Integer::intValue).sum();
        int plusOnePlusOneCountersBeforeEntry = permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (applyExileUncastEnteringCreature(gameData, controllerId, permanent)) {
            return;
        }
        if (!applyEntryCostReplacement(gameData, controllerId, permanent)) {
            return;
        }
        applySacrificeOtherPermanentsWithSameName(gameData, controllerId, permanent);
        Map<UUID, List<Permanent>> hidden = hideSimultaneouslyEntered(gameData, simultaneouslyEntered);
        try {
            if (sacrificeAllPermanentsAsEntersEffectHandler != null) {
                sacrificeAllPermanentsAsEntersEffectHandler.applyIfPresent(gameData, controllerId, permanent);
            }
            carrySpellTextReplacements(gameData, permanent);
            carrySpellColorOverride(gameData, controllerId, permanent);
            applyCreaturesEnterAsCopyReplacementEffect(gameData, controllerId, permanent);
            applyEnterTappedEffects(permanent, enterTappedTypes);
            applySelfEnterTapped(permanent);
            applyConditionalEnterTapped(gameData, controllerId, permanent);
            applyAllPermanentsEnterTapped(gameData, permanent);
            applyOpponentOnlyEnterTappedEffects(gameData, controllerId, permanent);
            applyUnchosenParityEnterTapped(gameData, permanent);
            applyEnterWithCounters(gameData, controllerId, permanent, xValue, kicked);
            applyGraveyardEnterWithAdditionalCounters(gameData, controllerId, permanent, simultaneouslyEntered);
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
        // CR 613.7b: a permanent receives its timestamp as it enters the battlefield.
        permanent.setTimestamp(gameData.nextTimestamp());
        gameData.playerBattlefields.get(controllerId).add(permanent);
        if (permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0) {
            permanentCounterSupport.firePlusOnePlusOneCounterTriggers(gameData, permanent);
        }
        // "Whenever a -1/-1 counter is put on a creature" (Flourishing Defenses) also sees a creature
        // that enters with -1/-1 counters (e.g. Leech Bonder, or persist) — CR ruling.
        permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                gameData, permanent, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE));
        gameData.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, k -> new ArrayList<>())
                .add(permanent.getCard());
        // Delayed "sacrifice this token at the beginning of the next end step" (Choreographed Sparks).
        if (permanent.getCard().isSacrificeAtEndStep()) {
            gameData.queueDelayedAction(new DelayedPermanentAction(permanent.getId(), DelayedPermanentActionKind.SACRIFICE_AT_END_STEP));
        }
        // "As this enters, you may reveal a [subtype] card from your hand; if you don't, it enters
        // tapped." Must run after the permanent is on the battlefield so we can reference/tap it.
        applyRevealSubtypeOrEntersTapped(gameData, controllerId, permanent);
        applyMayPayLifeOrEntersTapped(gameData, controllerId, permanent);
        applyUnleash(gameData, controllerId, permanent);
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
                || (!card.isToken() && !gameData.playersExilingUncastEnteringNontokenCreaturesThisTurn.isEmpty());
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
    private void applyRevealSubtypeOrEntersTapped(GameData gameData, UUID controllerId, Permanent permanent) {
        RevealSubtypeOrEntersTappedEffect effect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof RevealSubtypeOrEntersTappedEffect)
                .map(e -> (RevealSubtypeOrEntersTappedEffect) e)
                .findFirst().orElse(null);
        if (effect == null) {
            return;
        }
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean canReveal = hand != null && hand.stream()
                .anyMatch(c -> c.getSubtypes().contains(effect.subtype()));
        if (!canReveal) {
            permanent.tap();
            log.info("Game {} - {} enters tapped (no {} card to reveal)",
                    gameData.id, permanent.getCard().getName(), effect.subtype().getDisplayName());
            return;
        }
        gameData.pendingMayAbilities.add(new PendingMayAbility(
                permanent.getCard(),
                controllerId,
                List.of(effect),
                permanent.getCard().getName() + " — Reveal a " + effect.subtype().getDisplayName()
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
        putPermanentOntoBattlefield(gameData, controllerId, permanent);
    }

    public void completeSacrificePermanentsToEnter(GameData gameData, UUID controllerId, Permanent permanent,
                                                   boolean sacrificed) {
        if (!sacrificed) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, null);
            return;
        }
        permanent.setEntryCostPaid(true);
        putPermanentOntoBattlefield(gameData, controllerId, permanent);
    }

    public void completeDiscardCardToEnter(GameData gameData, UUID controllerId, Permanent permanent,
                                           boolean discarded) {
        if (!discarded) {
            putEnteringPermanentIntoGraveyard(gameData, controllerId, permanent, null);
            return;
        }
        permanent.setEntryCostPaid(true);
        putPermanentOntoBattlefield(gameData, controllerId, permanent);
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
                                        int xValue, boolean kicked) {
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
                        card, kicked, false, false, false, permanent.getCastFromZone(), xValue, null, null, false);
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(), conditionContext)) {
                    continue;
                }
                enterWith = wrapped;
            } else {
                continue;
            }

            if (permanent.getChosenSubtype() == null && isChosenSubtypeDependent(enterWith)) continue;
            applyEnterWithCountersEffect(gameData, controllerId, permanent, enterWith, xValue);
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

        for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof EnterWithCountersEffect enterWith && isChosenSubtypeDependent(enterWith)) {
                applyEnterWithCountersEffect(gameData, controllerId, permanent, enterWith, 0);
            }
        }
    }

    private void applyEnterWithCountersEffect(GameData gameData, UUID controllerId, Permanent permanent,
                                              EnterWithCountersEffect enterWith, int xValue) {
        int count = amountEvaluationService.evaluate(gameData, enterWith.count(),
                new AmountContext(controllerId, permanent, null, xValue, 0));
        if (enterWith.type() == CounterType.MINUS_ONE_MINUS_ONE
                && gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, permanent)) {
            count = 0;
        } else if (enterWith.type() == CounterType.PLUS_ONE_PLUS_ONE
                && gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent, controllerId)) {
            count = 0;
        }
        count = gameQueryService.replaceCounters(gameData, permanent, controllerId, enterWith.type(), count);
        if (count > 0) {
            permanent.setCounterCount(enterWith.type(), permanent.getCounterCount(enterWith.type()) + count);
            log.info("Game {} - {} enters with {} {} counter(s)",
                    gameData.id, permanent.getCard().getName(), count, enterWith.type());
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

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        triggerCollectionService.checkAllyTokenEntersTriggers(gameData, controllerId, count);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, etbMode, kicked, targetIds);
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                                 boolean wasCastFromHand, int etbMode, int xValue,
                                                 boolean kicked, List<UUID> targetIds) {
        // Track kicked status on the permanent for "if wasn't kicked" end-step triggers (e.g. Skizzik)
        if (kicked) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEnteredPerm = bf.get(bf.size() - 1);
            justEnteredPerm.setKicked(true);
        }

        // Tribute is chosen by an opponent as the creature enters. The choice is presented after
        // the permanent is placed so the shared may-ability interaction can carry it, but before
        // any ETB trigger is collected.
        TributeEffect tribute = card.getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof TributeEffect)
                .map(TributeEffect.class::cast)
                .findFirst().orElse(null);
        if (tribute != null) {
            Permanent justEntered = gameData.playerBattlefields.get(controllerId).getLast();
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            if (opponentId != null) {
                gameData.pendingMayAbilities.add(new PendingMayAbility(
                        card,
                        controllerId,
                        List.of(tribute),
                        card.getName() + " — Put " + tribute.counterCount()
                                + " +1/+1 counter(s) on it to pay tribute?",
                        null,
                        null,
                        justEntered.getId(),
                        null,
                        0,
                        0,
                        null,
                        null,
                        opponentId,
                        null));
                playerInputService.processNextMayAbility(gameData);
                return;
            }
        }

        // "As enters, choose another creature you control" — replacement effect (CR 614.1c),
        // not suppressed by Torpor Orb. Must happen before ETB triggers.
        boolean needsCreatureChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseAnotherCreatureOnEnterEffect);
        if (needsCreatureChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            List<UUID> validIds = bf.stream()
                    .filter(p -> p != justEntered && gameQueryService.isCreature(gameData, p))
                    .map(Permanent::getId)
                    .toList();
            if (!validIds.isEmpty()) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ChooseCreatureAsEnter(justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode, kicked));
                playerInputService.beginPermanentChoice(gameData, controllerId, new ArrayList<>(validIds), "Choose another creature you control.");
                return;
            }
            // No other creatures — bodyguard enters with no chosen creature
        }

        boolean needsPrimalClayFormChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChoosePrimalClayFormOnEnterEffect);
        if (needsPrimalClayFormChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginPrimalClayFormChoice(gameData, controllerId, justEntered.getId());
            return;
        }

        ChooseColorEffect colorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseColorEffect)
                .map(e -> (ChooseColorEffect) e)
                .findFirst().orElse(null);
        if (colorChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), targetId,
                    colorChoice);
            return;
        }

        // "As this creature enters, choose a basic land type" — a choice made during entry
        // (CR 614.1c), before ETB triggers; the choice handler resumes them once made. Realmwright.
        ChooseBasicLandTypeOnEnterEffect landTypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseBasicLandTypeOnEnterEffect)
                .map(e -> (ChooseBasicLandTypeOnEnterEffect) e)
                .findFirst().orElse(null);
        if (landTypeChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginBasicLandTypeChoice(gameData, controllerId, justEntered.getId(),
                    false, landTypeChoice.choicesRequired() > 1, landTypeChoice.allowedTypes());
            return;
        }

        // "As this creature enters, choose a creature type" — a choice made during entry
        // (CR 614.1c), before ETB triggers; the choice handler resumes them once made.
        ChooseSubtypeOnEnterEffect subtypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseSubtypeOnEnterEffect.class::isInstance)
                .map(ChooseSubtypeOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (subtypeChoice != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginSubtypeChoice(gameData, controllerId, justEntered.getId(),
                    subtypeChoice.allowedSubtypes());
            return;
        }

        // Devour (CR 702.82): "As this creature enters, you may sacrifice any number of creatures.
        // It enters with N times that many +1/+1 counters on it." As-enters replacement, resolved
        // before ETB triggers. Prompt the controller to sacrifice any of their other creatures.
        DevourEffect devour = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof DevourEffect)
                .map(e -> (DevourEffect) e)
                .findFirst().orElse(null);
        if (devour != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered && gameQueryService.isCreature(gameData, p))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.DevourSacrifice(justEntered.getId(), devour.multiplier(),
                                controllerId, card, targetId, wasCastFromHand, etbMode, kicked),
                        card.getName() + " — Devour: sacrifice any number of creatures.");
                return;
            }
            // No other creatures — devours nothing; ETB triggers proceed with 0 devoured creatures.
        }

        // "As this creature enters, sacrifice any number of creatures. This creature's power becomes
        // their total power and its toughness their total toughness" (CR 614.1c, Dracoplasm).
        boolean needsSacrificeForPowerToughness = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof SacrificeAnyNumberOfCreaturesSetPowerToughnessOnEnterEffect);
        if (needsSacrificeForPowerToughness) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered && gameQueryService.isCreature(gameData, p))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.SacrificeCreaturesSetEnteringPowerToughness(
                                justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode, kicked),
                        card.getName() + " — sacrifice any number of creatures.");
                return;
            }
            // No other creatures — nothing is sacrificed; the creature enters as a 0/0.
        }

        // "As this creature enters, sacrifice any number of permanents. It enters with that many
        // +1/+1 counters on it" (CR 614.1c, Shimatsu the Bloodcloaked). Resolved before ETB triggers;
        // the entering permanent itself isn't offered.
        SacrificePermanentsAsEntersForCountersEffect sacForCounters =
                card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .filter(e -> e instanceof SacrificePermanentsAsEntersForCountersEffect)
                        .map(e -> (SacrificePermanentsAsEntersForCountersEffect) e)
                        .findFirst().orElse(null);
        if (sacForCounters != null) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            FilterContext filterContext = FilterContext.of(gameData)
                    .withSourceCardId(card.getId())
                    .withSourceControllerId(controllerId);
            List<UUID> sacrificeable = bf.stream()
                    .filter(p -> p != justEntered)
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(
                            p, sacForCounters.filter(), filterContext))
                    .map(Permanent::getId)
                    .toList();
            if (!sacrificeable.isEmpty()) {
                playerInputService.beginMultiPermanentChoice(gameData, controllerId,
                        new ArrayList<>(sacrificeable), sacrificeable.size(),
                        new MultiPermanentChoiceContext.SacrificeAsEntersForCounters(justEntered.getId(),
                                sacForCounters.countersPerPermanent(), controllerId, card, targetId,
                                wasCastFromHand, etbMode, kicked),
                        card.getName() + " — sacrifice any number of permanents.");
                return;
            }
            // Nothing to sacrifice — it enters with no counters; ETB triggers proceed.
        }

        // "As this creature enters, pay any amount of life" (Minion of the Wastes). The payment is a
        // choice made during entry, before ETB triggers; the amount is stored on the permanent so a
        // characteristic-defining power/toughness can read it back.
        boolean needsLifePayment = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof PayAnyAmountOfLifeOnEnterEffect);
        if (needsLifePayment) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            int life = gameData.playerLifeTotals.getOrDefault(controllerId, 0);
            playerInputService.beginPayAnyAmountOfLifeChoice(gameData, controllerId, life,
                    new ChoiceContext.PayAnyAmountOfLifeAsEnters(justEntered.getId(), controllerId, card,
                            targetId, wasCastFromHand, etbMode, kicked));
            return;
        }

        // "As this creature enters, exile any number of creature cards from your graveyard"
        // (CR 614.1c, Sutured Ghoul). The exiled cards are tracked with the entering permanent so
        // its characteristic-defining power/toughness can be derived from them.
        boolean needsGraveyardExile = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ExileAnyNumberOfCreatureCardsFromGraveyardOnEnterEffect);
        if (needsGraveyardExile) {
            List<Card> creatureCards = gameData.playerGraveyards
                    .getOrDefault(controllerId, List.of()).stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .toList();
            if (!creatureCards.isEmpty()) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                Permanent justEntered = bf.get(bf.size() - 1);
                gameData.graveyardTargetOperation.asEntersExile =
                        new GraveyardTargetOperationState.AsEntersGraveyardExileContext(
                                justEntered.getId(), controllerId, card, targetId, wasCastFromHand, etbMode, kicked);
                playerInputService.beginMultiGraveyardChoice(gameData, controllerId,
                        new ArrayList<>(creatureCards), creatureCards.size(),
                        card.getName() + " — Exile any number of creature cards from your graveyard.");
                return;
            }
            // Empty graveyard — nothing is exiled; the creature enters with 0 total power/toughness.
        }

        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, List<UUID> targetIds) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void processLandETBEffects(GameData gameData, UUID controllerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        Permanent enteringPermanent = battlefield != null && !battlefield.isEmpty() ? battlefield.getLast() : null;
        ChooseSubtypeOnEnterEffect subtypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseSubtypeOnEnterEffect.class::isInstance)
                .map(ChooseSubtypeOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && enteringPermanent.getChosenSubtype() == null && subtypeChoice != null) {
            playerInputService.beginSubtypeChoice(gameData, controllerId, enteringPermanent.getId(),
                    subtypeChoice.allowedSubtypes(), true);
            return;
        }
        processCreatureETBEffects(gameData, controllerId, card, null, false);
    }

    public void processFaceDownCreatureETBTriggers(GameData gameData, UUID controllerId, Card card) {
        if (gameQueryService.areCreatureETBTriggersSuppressed(gameData, card)) {
            log.info("Game {} - {} ETB triggers suppressed (creature entering triggers disabled)", gameData.id, card.getName());
            return;
        }

        processCreatureEntersTriggers(gameData, controllerId, card, 0, true);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, etbMode, kicked, targetIds);
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        Permanent enteringPermanent = battlefield != null && !battlefield.isEmpty() ? battlefield.getLast() : null;
        ChooseSubtypeOnEnterEffect subtypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseSubtypeOnEnterEffect.class::isInstance)
                .map(ChooseSubtypeOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && enteringPermanent.getChosenSubtype() == null && subtypeChoice != null) {
            playerInputService.beginSubtypeChoice(gameData, controllerId, enteringPermanent.getId(),
                    subtypeChoice.allowedSubtypes());
            return;
        }

        // Torpor Orb: "Creatures entering don't cause abilities to trigger."
        if (gameQueryService.areCreatureETBTriggersSuppressed(gameData, card)) {
            log.info("Game {} - {} ETB triggers suppressed (creature entering triggers disabled)", gameData.id, card.getName());
            return;
        }

        int extraEtbTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, card);

        List<CardEffect> triggeredEffects = new ArrayList<>(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD));
        int additionalElementalTriggers = enteringPermanent == null ? 0
                : gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, controllerId, enteringPermanent);
        int extraTriggerCopies = extraEtbTriggers + additionalElementalTriggers;
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard() == card) {
                    triggeredEffects.addAll(triggerCollectionService.grantedTriggeredEffects(
                            gameData, permanent, EffectSlot.ON_ENTER_BATTLEFIELD));
                    break;
                }
            }
        }
        triggeredEffects = triggeredEffects.stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
                // Primal Clay's shape choice is made while the new permanent enters, not as an ETB ability.
                .filter(e -> !(e instanceof ChoosePrimalClayFormOnEnterEffect))
                // "As enters, choose a creature type" is a replacement-style choice made during entry
                // (handled via beginSubtypeChoice), not a triggered ability queued onto the stack.
                .filter(e -> !(e instanceof ChooseSubtypeOnEnterEffect))
                .filter(e -> !(e instanceof ReplacementEffect))
                // Conditional as-enters replacements ("if kicked, enters with N counters") are
                // handled during entry, not by the triggered-ability pipeline.
                .filter(e -> !(e instanceof ConditionalEffect conditional
                        && conditional.wrapped() instanceof ReplacementEffect))
                .toList();
        if (!triggeredEffects.isEmpty()) {
            // Extract per-mode targetFilter from ChooseOneEffect (if present)
            TargetFilter modeTargetFilter = null;
            for (CardEffect e : triggeredEffects) {
                if (e instanceof ChooseOneEffect coe) {
                    int idx = (etbMode >= 0 && etbMode < coe.options().size()) ? etbMode : 0;
                    modeTargetFilter = coe.options().get(idx).targetFilter();
                    break;
                }
            }

            List<CardEffect> mayEffects = triggeredEffects.stream().filter(e -> e instanceof MayEffect).toList();
            // Evoke sacrifice gate (CR 603.4): read the just-entered permanent's evoked flag, which
            // was stamped from the spell's cast context at resolution time.
            List<Permanent> evokeBf = gameData.playerBattlefields.get(controllerId);
            boolean evoked = evokeBf != null && !evokeBf.isEmpty() && evokeBf.getLast().isEvoked();
            // Prowl gate (CR 603.4): read the just-entered permanent's prowl flag, stamped from the
            // spell's cast context at resolution time.
            boolean prowl = evokeBf != null && !evokeBf.isEmpty() && evokeBf.getLast().isProwl();
            // Resolve each mandatory effect into its trigger-time form: modal unwrap, value
            // materialisation, and intervening-if gating (CR 603.4) — a null result drops the trigger.
            EtbEffectContext etbCtx = new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode,
                    kicked, evoked, prowl, enteringPermanent);
            List<CardEffect> mandatoryEffects = triggeredEffects.stream()
                    .filter(e -> !(e instanceof MayEffect))
                    .map(e -> etbEffectResolver.resolve(etbCtx, e))
                    .filter(Objects::nonNull)
                    .toList();

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                // CR 603.3c: a "may [do X to] target permanent" ETB (e.g. Leonin Relic-Warder)
                // targets, so with no legal target the ability isn't put onto the stack at all —
                // the controller isn't even prompted. Skip queueing it in that case.
                if (mayEtbTargetsPermanentButHasNoLegalTarget(gameData, controllerId, card, may)) {
                    log.info("Game {} - {} may ETB ability not put on stack (no legal targets)",
                            gameData.id, card.getName());
                    continue;
                }
                TargetSpec mayTargetSpec = may.targetSpec();
                if (mayTargetSpec.admits(TargetPredicate.Kind.PERMANENT)
                        || mayTargetSpec.admits(TargetPredicate.Kind.PLAYER)
                        || mayTargetSpec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                            List.of(may), modeTargetFilter, extraTriggerCopies, etbMode, xValue);
                    continue;
                }
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;
                gameData.queueMayAbility(card, controllerId, may, null, sourcePermanentId);
                // Naban: extra triggers for Wizard ETB
                for (int i = 0; i < extraTriggerCopies; i++) {
                    gameData.queueMayAbility(card, controllerId, may, null, sourcePermanentId);
                }
            }

            if (!mandatoryEffects.isEmpty()) {
                queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                        mandatoryEffects, modeTargetFilter, extraTriggerCopies, etbMode, xValue);
            }
        }

        processCreatureEntersTriggers(gameData, controllerId, card, extraEtbTriggers, false);
    }

    private void processCreatureEntersTriggers(GameData gameData, UUID controllerId, Card card,
                                               int extraEtbTriggers, boolean faceDown) {
        triggerCollectionService.checkAllyCreatureEntersTriggers(gameData, controllerId, card, extraEtbTriggers);
        triggerCollectionService.checkAllyNontokenCreatureEntersTriggers(gameData, controllerId, card);
        if (!faceDown) {
            triggerCollectionService.checkAllyArtifactEntersTriggers(gameData, controllerId, card);
            triggerCollectionService.checkAllyEquipmentEntersTriggers(gameData, controllerId, card);
            triggerCollectionService.checkAllyEnchantmentEntersTriggers(gameData, controllerId, card);
            triggerCollectionService.checkAllyNontokenArtifactEntersTriggers(gameData, controllerId, card);
        }
        triggerCollectionService.checkOpponentCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAnyCreatureEntersTriggers(gameData, controllerId, card, extraEtbTriggers);
        triggerCollectionService.checkCreatureEntersThisTurnTriggers(gameData, card);
        triggerCollectionService.checkAnyPermanentEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkEnchantedPlayerCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkEntersFromGraveyardTriggers(gameData, controllerId, card);
        triggerCollectionService.checkPermanentEntersFromGraveyardTriggers(gameData, controllerId, card);
        triggerCollectionService.checkSelfEntersFromGraveyardTriggers(gameData, controllerId, card);
        if (!faceDown && card.hasType(CardType.LAND)) {
            triggerCollectionService.checkOpponentLandEntersTriggers(gameData, controllerId, card);
            triggerCollectionService.checkAllyLandEntersTriggers(gameData, controllerId, card);
        }
    }

    /**
     * True when a "may" ETB ability targets a permanent (and only a permanent) via a concrete
     * predicate filter but no permanent on the battlefield satisfies it — meaning the targeted
     * triggered ability has no legal target and must not be put onto the stack (CR 603.3c).
     *
     * <p>Deliberately narrow: it mirrors the pure permanent-target branch of
     * {@code MayAbilityHandlerService.handleTargetedMayAbilityAccepted}. Abilities that can also
     * target a player (a player is always a legal target), that target a graveyard card (resolved
     * on a separate path), or that lack a {@link PermanentPredicateTargetFilter} (e.g. Clone-style
     * copy effects, which don't target) are left untouched and queue as before.
     */
    private boolean mayEtbTargetsPermanentButHasNoLegalTarget(GameData gameData, UUID controllerId,
                                                              Card card, MayEffect may) {
        CardEffect wrapped = may.wrapped();
        TargetSpec wrappedSpec = wrapped.targetSpec();
        if (!wrappedSpec.admits(TargetPredicate.Kind.PERMANENT)
                || wrappedSpec.admits(TargetPredicate.Kind.PLAYER)
                || wrappedSpec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
            return false;
        }
        PermanentPredicate effectPredicate = EffectResolution.targetPredicateOf(wrapped);
        TargetFilter targetFilter = card.getTargetFilter();
        if (targetFilter == null && effectPredicate == null) {
            return false;
        }
        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(card.getId())
                .withSourceControllerId(controllerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                boolean matches = effectPredicate == null
                        || predicateEvaluationService.matchesPermanentPredicate(p, effectPredicate, ctx);
                if (matches && targetFilter != null) {
                    matches = predicateEvaluationService.checkTargetFilter(targetFilter, p, ctx).isEmpty();
                }
                if (matches) return false;
            }
        }
        return true;
    }

    /**
     * Routes the already-resolved mandatory ETB effects to the stack or to the appropriate
     * interactive target-selection queue, and processes any pending queue that isn't already
     * awaiting input. The effects have already been unwrapped/gated by {@link EtbEffectResolver}.
     *
     * <p>Effects are partitioned by the kind of target selection they need at trigger time:
     * graveyard-exile (multi-target), graveyard-cast and grant-flashback (single graveyard target),
     * spell-targeting (choose a spell on the stack), and everything else ("other"), which either
     * goes straight onto the stack (target already chosen at cast time) or, for token copies and
     * permanents that entered from a graveyard, is queued to choose targets as the ability goes on
     * the stack (CR 603.3b). Trigger-copy effects apply to every path via {@code extraTriggerCopies}.
     */
    private void queueMandatoryETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          List<UUID> targetIds, List<CardEffect> mandatoryEffects,
                                          TargetFilter modeTargetFilter, int extraTriggerCopies,
                                          int etbMode, int xValue) {
        // Separate graveyard exile effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardExileEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileCardsFromGraveyardEffect).toList();
        // Separate targeted graveyard-card exile effects (Disposal Mummy: "exile target card from an
        // opponent's graveyard"). Distinct from the whole-set ExileCardsFromGraveyardEffect above: the
        // scope decides which graveyards are searched, and targets are chosen at trigger time.
        List<CardEffect> graveyardCardsExileEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileGraveyardCardsEffect ege && ege.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .toList();
        // Separate mixed-zone exile effects (Angel of Serenity: "up to three other target creatures
        // from the battlefield and/or creature cards from graveyards"): one card pool spanning both
        // zones, chosen at trigger time.
        List<CardEffect> mixedZoneChoiceEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof BattlefieldAndGraveyardCardChoosingEffect).toList();
        // Separate graveyard cast effects (need single-target selection at trigger time)
        List<CardEffect> graveyardCastEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .filter(e -> card.getEffectTargetIndex(e) < 0)
                .toList();
        // Separate graveyard flashback-grant effects (need single-target selection at trigger time)
        List<CardEffect> graveyardFlashbackEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect).toList();
        // Separate graveyard exile-and-may-play effects (need single-target selection at trigger time)
        List<CardEffect> graveyardMayPlayEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect).toList();
        // Separate opponent-graveyard steal effects (need single-target selection at trigger time)
        List<CardEffect> graveyardStealEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect
                        || e instanceof PutCardFromOpponentGraveyardOntoBattlefieldEffect).toList();
        // Separate graveyard return-to-hand effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardReturnToHandEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ReturnTargetCardsFromGraveyardToHandEffect).toList();
        // Separate effects that first target a player and then choose cards from that player's graveyard.
        List<CardEffect> targetPlayerGraveyardChoiceEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof GraveyardCardChoosingEffect
                        && e.targetSpec().admits(TargetPredicate.Kind.PLAYER)).toList();
        // Separate controller-graveyard shuffle-into-library effects (multi-target at trigger time)
        List<CardEffect> graveyardShuffleIntoLibraryEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect).toList();
        // Separate targeted graveyard-return effects (e.g. Bladewing the Risen: "return target Dragon
        // permanent card from your graveyard to the battlefield"): any remaining graveyard-target effect
        // not covered by the specialized paths above. Its target is chosen as the trigger goes on the
        // stack via the shared SpellGraveyardTargetTrigger flow (identified by target category, not by
        // concrete effect type, so a new graveyard-target effect needs no branch here).
        List<CardEffect> graveyardTargetReturnEffects = mandatoryEffects.stream()
                .filter(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD))
                .filter(e -> !graveyardExileEffects.contains(e))
                .filter(e -> !graveyardCardsExileEffects.contains(e))
                .filter(e -> !(e instanceof CastTargetInstantOrSorceryFromGraveyardEffect))
                .filter(e -> !graveyardCastEffects.contains(e))
                .filter(e -> !graveyardFlashbackEffects.contains(e))
                .filter(e -> !graveyardMayPlayEffects.contains(e))
                .filter(e -> !graveyardStealEffects.contains(e))
                .filter(e -> !graveyardReturnToHandEffects.contains(e))
                .filter(e -> !graveyardShuffleIntoLibraryEffects.contains(e))
                .toList();
        List<CardEffect> otherEffects = mandatoryEffects.stream()
                .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect))
                .filter(e -> !graveyardCastEffects.contains(e))
                .filter(e -> !(e instanceof GrantFlashbackToTargetGraveyardCardEffect))
                .filter(e -> !(e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect))
                .filter(e -> !graveyardStealEffects.contains(e))
                .filter(e -> !(e instanceof ReturnTargetCardsFromGraveyardToHandEffect))
                .filter(e -> !targetPlayerGraveyardChoiceEffects.contains(e))
                .filter(e -> !(e instanceof ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect))
                .filter(e -> !graveyardTargetReturnEffects.contains(e))
                .filter(e -> !graveyardCardsExileEffects.contains(e))
                .filter(e -> !mixedZoneChoiceEffects.contains(e))
                .filter(e -> !EffectResolution.targetsSpellOnStack(e)).toList();
        // Separate spell-targeting effects (need stack-target selection at trigger time)
        List<CardEffect> spellTargetEffects = mandatoryEffects.stream()
                .filter(EffectResolution::targetsSpellOnStack).toList();

        // Put non-special effects on the stack as before
        if (!otherEffects.isEmpty()) {
            List<UUID> activeTargetIds = targetsForActiveEtbGroups(card, otherEffects, targetIds);
            boolean hasTarget = targetId != null || !activeTargetIds.isEmpty();

            // A permanent that entered without a target chosen at cast time — a token copy,
            // a creature put onto the battlefield from a graveyard via undying / reanimation,
            // or a land (lands are played, never cast, so they never went through cast-time
            // target selection; e.g. Sunscorched Desert's "deals 1 damage to target player or
            // planeswalker") — must still choose targets for its mandatory ETB as the ability is
            // put on the stack (CR 603.3b). Cast spells with "up to" targets that chose 0 targets
            // are excluded; they passed through cast-time target selection.
            boolean hasDynamicTargetCount = card.hasDynamicTargetCount();
            boolean etbNeedsTarget = otherEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));

            // A surviving gate-conditional ETB (Metalcraft, Morbid, Raid, … — the gate was met
            // as the permanent entered) that targets never chose a target at cast time
            // (CR 601.2c): it is excluded from cast-time targeting by EffectResolution, so the
            // controller picks the target as the trigger goes on the stack (CR 603.3d), on the
            // same deferred path token copies and reanimated permanents use. A stale targetId
            // from the cast is deliberately ignored — the engine never asked for it.
            boolean gateConditionalNeedsTarget = otherEffects.stream()
                    .anyMatch(e -> e instanceof ConditionalEffect ce && ce.condition().isEtbTriggerGate()
                            && (ce.targetSpec().admits(TargetPredicate.Kind.PLAYER) || ce.targetSpec().admits(TargetPredicate.Kind.PERMANENT)));

            // MayPayManaEffect ETBs never take a cast-time target (see EffectResolution), so a
            // targeting pay/else ability (Knight of the Mists) must choose as the trigger goes
            // on the stack — including the just-entered permanent as a legal choice.
            boolean mayPayManaNeedsTarget = otherEffects.stream()
                    .anyMatch(e -> e instanceof MayPayManaEffect
                            && (e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                            || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)));

            boolean auraETBTargetNeedsSelection = card.isAura()
                    && targetIds.isEmpty()
                    && otherEffects.stream().anyMatch(e -> card.getEffectTargetIndex(e) > 0
                    && (e.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                    || e.targetSpec().admits(TargetPredicate.Kind.PERMANENT)));

            if ((hasDynamicTargetCount && !hasTarget)
                    || gateConditionalNeedsTarget
                    || mayPayManaNeedsTarget
                    || auraETBTargetNeedsSelection
                    || (etbNeedsTarget && !hasTarget)) {
                // CR 603.3: no target was chosen at cast time — the ETB target is gated behind
                // an intervening-if, or the permanent wasn't cast (token copy, or returned from
                // a graveyard via undying / reanimation). The controller must choose a target
                // as the triggered ability is put on the stack.
                // For non-token casts with "up to N" abilities where 0 was chosen,
                // the ETB still triggers but has no effect — we skip queueing it.
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                if (card.getSpellTargets().size() > 1 || etbTokenTargetService.needsSlotBySlotTargetSelection(card)) {
                    // Multi-target ETB (e.g. Burning Sun's Avatar, or a single group with
                    // "up to N" targets): choose slot-by-slot at trigger time,
                    // accumulating into targetIds.
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                            List.of(), card.isAura() ? 1 : 0, 0,
                            card.isAura() ? List.of(1) : List.of()));
                    for (int i = 0; i < extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                                List.of(), card.isAura() ? 1 : 0, 0,
                                card.isAura() ? List.of(1) : List.of()));
                    }
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s enter-the-battlefield ability triggers — choose targets."));
                    log.info("Game {} - {} ETB multi-target trigger queued (no target chosen at cast time)",
                            gameData.id, card.getName());
                } else {
                    TargetFilter etbTargetFilter = modeTargetFilter != null ? modeTargetFilter : card.getTargetFilter();

                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter));
                    for (int i = 0; i < extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter));
                    }
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, "'s enter-the-battlefield ability triggers — choose a target."));
                    log.info("Game {} - {} ETB trigger queued for target selection (no target chosen at cast time)",
                            gameData.id, card.getName());
                }
            } else if (!etbNeedsTarget || hasTarget) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                // Snapshot the paid X onto the ETB stack entry so DynamicAmount XValue effects read
                // the cast context on resolution.
                StackEntry etbEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ETB ability",
                        new ArrayList<>(otherEffects),
                        xValue,
                        targetId,
                        sourcePermanentId,
                        Map.of(),
                        null,
                        List.of(),
                        activeTargetIds
                );
                if (modeTargetFilter != null) {
                    etbEntry.setTargetFilter(modeTargetFilter);
                }
                gameData.stack.add(etbEntry);
                gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
                log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                // Naban: extra triggers for Wizard ETB
                for (int i = 0; i < extraTriggerCopies; i++) {
                    StackEntry extraEtbEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            controllerId,
                            card.getName() + "'s ETB ability",
                            new ArrayList<>(otherEffects),
                            xValue,
                            targetId,
                            sourcePermanentId,
                            Map.of(),
                            null,
                            List.of(),
                            activeTargetIds
                    );
                    if (modeTargetFilter != null) {
                        extraEtbEntry.setTargetFilter(modeTargetFilter);
                    }
                    gameData.stack.add(extraEtbEntry);
                    gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability triggers."));
                    log.info("Game {} - {} ETB ability pushed onto stack (Wizard ETB extra trigger)", gameData.id, card.getName());
                }
            }
        }

        // Handle effects that target a player and then choose cards from that player's graveyard.
        for (CardEffect effect : targetPlayerGraveyardChoiceEffects) {
            List<Permanent> enteredBattlefield = gameData.playerBattlefields.get(controllerId);
            UUID sourcePermanentId = enteredBattlefield == null || enteredBattlefield.isEmpty()
                    ? null : enteredBattlefield.getLast().getId();
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                TargetFilter etbTargetFilter = modeTargetFilter != null ? modeTargetFilter : card.getTargetFilter();
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                        card, controllerId, List.of(effect), sourcePermanentId, etbTargetFilter));
            }
        }

        // Handle graveyard exile effects: targets must be chosen at trigger time
        for (CardEffect effect : graveyardExileEffects) {
            ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGraveyardExileETBTargeting(gameData, controllerId, card, mandatoryEffects, exile);
            }
        }

        // Handle targeted graveyard-card exile effects (opponent's/any graveyard, e.g. Disposal Mummy):
        // choose the graveyard target as the trigger goes on the stack.
        for (CardEffect effect : graveyardCardsExileEffects) {
            ExileGraveyardCardsEffect exile = (ExileGraveyardCardsEffect) effect;
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGraveyardCardsExileETBTargeting(gameData, controllerId, card, List.of(effect), exile);
            }
        }

        // Handle mixed-zone exile effects: one selection across battlefield creatures and graveyard
        // creature cards, chosen as the trigger goes on the stack.
        for (CardEffect effect : mixedZoneChoiceEffects) {
            List<Permanent> mixedZoneBf = gameData.playerBattlefields.get(controllerId);
            UUID mixedZoneSourceId = mixedZoneBf != null && !mixedZoneBf.isEmpty()
                    ? mixedZoneBf.getLast().getId() : null;
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleBattlefieldAndGraveyardExileETBTargeting(gameData, controllerId,
                        card, List.of(effect), mixedZoneSourceId,
                        (BattlefieldAndGraveyardCardChoosingEffect) effect);
            }
        }

        // Handle graveyard cast effects: target instant/sorcery in opponent's graveyard
        for (CardEffect effect : graveyardCastEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGraveyardCastETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard flashback-grant effects: target instant/sorcery in controller's graveyard
        for (CardEffect effect : graveyardFlashbackEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGrantFlashbackETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard exile-and-may-play effects: target card in controller's graveyard
        for (CardEffect effect : graveyardMayPlayEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGraveyardMayPlayETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle opponent-graveyard steal effects: target creature card in an opponent's graveyard
        for (CardEffect effect : graveyardStealEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handlePutCreatureFromOpponentGraveyardETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard return-to-hand effects: up to N target cards in controller's graveyard
        for (CardEffect effect : graveyardReturnToHandEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleReturnToHandETBTargeting(gameData, controllerId, card,
                        List.of(effect), (ReturnTargetCardsFromGraveyardToHandEffect) effect);
            }
        }

        // Handle shuffle-into-library effects: up to N target cards in controller's graveyard
        for (CardEffect effect : graveyardShuffleIntoLibraryEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleShuffleIntoLibraryETBTargeting(gameData, controllerId, card,
                        List.of(effect), (ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect) effect);
            }
        }

        // Handle targeted graveyard-return effects (return target card from your graveyard to the
        // battlefield/hand): choose the graveyard target as the trigger goes on the stack, reusing the
        // shared SpellGraveyardTargetTrigger flow. Optional effects use an up-to-one selection.
        int minimumGraveyardTargets = graveyardTargetReturnEffects.stream()
                .anyMatch(effect -> !(effect instanceof ReturnCardFromGraveyardEffect returnEffect)
                        || !returnEffect.upTo()) ? 1 : 0;
        for (CardEffect effect : graveyardTargetReturnEffects) {
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                        card, controllerId, List.of(effect), null, minimumGraveyardTargets));
            }
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
        }

        // Handle spell-targeting ETB effects: target must be chosen from spells on the stack
        for (CardEffect effect : spellTargetEffects) {
            StackEntryPredicate spellFilter = null;
            boolean includeAbilities = false;
            if (effect instanceof CopySpellEffect cse) {
                spellFilter = cse.spellFilter();
            } else if (card.getTargetFilter() instanceof StackEntryPredicateTargetFilter sf) {
                // "counter target spell with mana value X or less" (Spellstutter Sprite): the
                // legal-spell restriction lives on the card's target filter, not the effect.
                spellFilter = sf.predicate();
                // "target spell or ability" (Mizzium Meddler) is signalled by a has-target filter.
                includeAbilities = TriggerCollectionService.predicateContainsHasTarget(sf.predicate());
            }
            List<Permanent> entered = gameData.playerBattlefields.get(controllerId);
            UUID sourcePermanentId = entered == null ? null : entered.stream()
                    .filter(p -> p.getCard().getId().equals(card.getId()))
                    .map(Permanent::getId)
                    .findFirst().orElse(null);
            gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                    card, controllerId, List.of(effect), spellFilter, includeAbilities, sourcePermanentId));
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBSpellTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenTargetTrigger(gameData);
        }
        if (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)
                && !gameData.interaction.isAwaitingInput()) {
            etbTokenTargetService.processNextETBTokenMultiTargetTrigger(gameData);
        }
    }

    private List<UUID> targetsForActiveEtbGroups(Card card, List<CardEffect> effects, List<UUID> targetIds) {
        if (targetIds == null || targetIds.isEmpty() || card.getSpellTargets().isEmpty()) {
            return targetIds == null ? List.of() : targetIds;
        }
        List<UUID> activeTargets = new ArrayList<>();
        int consumed = 0;
        for (SpellTarget group : card.getSpellTargets()) {
            if (card.isAura() && group.getIndex() == 0) {
                continue;
            }
            int size = Math.min(group.getMaxTargets(), targetIds.size() - consumed);
            if (size <= 0) {
                break;
            }
            boolean active = !card.bindsEffectToTargetGroup(group.getIndex())
                    || effects.stream().anyMatch(effect -> card.getEffectTargetIndex(effect) == group.getIndex());
            if (active) {
                activeTargets.addAll(targetIds.subList(consumed, consumed + size));
            }
            consumed += size;
        }
        return List.copyOf(activeTargets);
    }
}
