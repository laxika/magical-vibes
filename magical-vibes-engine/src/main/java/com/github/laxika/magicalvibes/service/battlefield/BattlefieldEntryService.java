package com.github.laxika.magicalvibes.service.battlefield;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndStep;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetCategory;
import com.github.laxika.magicalvibes.model.effect.CantHaveCountersEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseAnotherCreatureOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesOfUnchosenParityEnterTappedEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesEnterAsCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.EnterPermanentsOfTypesTappedEffect;
import com.github.laxika.magicalvibes.model.effect.EnterWithCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardEnterWithAdditionalCountersEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.RevealSubtypeOrEntersTappedEffect;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectContext;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class BattlefieldEntryService {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
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

    // @Lazy on triggerCollectionService and permanentCounterSupport breaks the constructor cycle:
    // BattlefieldEntryService → TriggerCollectionService/PermanentCounterSupport →
    // PlayerInputService/queue services → (effect handlers) → BattlefieldEntryService.
    public BattlefieldEntryService(GameQueryService gameQueryService,
                                   GameBroadcastService gameBroadcastService,
                                   PlayerInputService playerInputService,
                                   PermanentCopierService permanentCopierService,
                                   @Lazy TriggerCollectionService triggerCollectionService,
                                   GraveyardTargetingService graveyardTargetingService,
                                   ETBTokenTargetService etbTokenTargetService,
                                   EtbEffectResolver etbEffectResolver,
                                   AmountEvaluationService amountEvaluationService,
                                   ConditionEvaluationService conditionEvaluationService,
                                   PredicateEvaluationService predicateEvaluationService,
                                   @Lazy com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport permanentCounterSupport) {
        this.gameQueryService = gameQueryService;
        this.gameBroadcastService = gameBroadcastService;
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
        carrySpellTextReplacements(gameData, permanent);
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
        // CR 613.7b: a permanent receives its timestamp as it enters the battlefield.
        permanent.setTimestamp(gameData.nextTimestamp());
        gameData.playerBattlefields.get(controllerId).add(permanent);
        // "Whenever a -1/-1 counter is put on a creature" (Flourishing Defenses) also sees a creature
        // that enters with -1/-1 counters (e.g. Leech Bonder, or persist) — CR ruling.
        permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(
                gameData, permanent, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE));
        gameData.permanentsEnteredBattlefieldThisTurn
                .computeIfAbsent(controllerId, k -> new ArrayList<>())
                .add(permanent.getCard());
        // Delayed "sacrifice this token at the beginning of the next end step" (Choreographed Sparks).
        if (permanent.getCard().isSacrificeAtEndStep()) {
            gameData.queueDelayedAction(new SacrificeAtEndStep(permanent.getId()));
        }
        // "As this enters, you may reveal a [subtype] card from your hand; if you don't, it enters
        // tapped." Must run after the permanent is on the battlefield so we can reference/tap it.
        applyRevealSubtypeOrEntersTapped(gameData, controllerId, permanent);
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

    /**
     * CR 614.1c — "Creatures you control enter as a copy of this creature."
     * If the entering permanent is a creature and the controller has a permanent with
     * {@link CreaturesEnterAsCopyOfSourceEffect}, the entering creature becomes a copy
     * of that source permanent. This is mandatory (not a "may" ability).
     */
    private void applyCreaturesEnterAsCopyReplacementEffect(GameData gameData, UUID controllerId, Permanent entering) {
        if (!entering.getCard().hasType(CardType.CREATURE)) {
            return;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return;

        for (Permanent source : battlefield) {
            boolean hasEffect = source.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof CreaturesEnterAsCopyOfSourceEffect);
            if (hasEffect) {
                permanentCopierService.applyCloneCopy(entering, source, null, null);
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
                    if (matchesAnyType(enteringPermanent.getCard(), enterTapped.cardTypes())) {
                        enteringPermanent.tap();
                    }
                }
            }
        });
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
        boolean cantHaveCounters = card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof CantHaveCountersEffect);
        if (cantHaveCounters) return;

        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            EnterWithCountersEffect enterWith;
            if (effect instanceof EnterWithCountersEffect direct) {
                enterWith = direct;
            } else if (effect instanceof ConditionalEffect conditional
                    && conditional.wrapped() instanceof EnterWithCountersEffect wrapped) {
                ConditionContext conditionContext = new ConditionContext(controllerId, null, permanent,
                        card, kicked, false, null, xValue, null, null, false);
                if (!conditionEvaluationService.isMet(gameData, conditional.condition(), conditionContext)) {
                    continue;
                }
                enterWith = wrapped;
            } else {
                continue;
            }

            int count = amountEvaluationService.evaluate(gameData, enterWith.count(),
                    new AmountContext(controllerId, permanent, null, xValue, 0, false));
            if (count > 0) {
                permanent.setCounterCount(enterWith.type(), permanent.getCounterCount(enterWith.type()) + count);
                log.info("Game {} - {} enters with {} {} counter(s)",
                        gameData.id, card.getName(), count, enterWith.type());
            }
        }
    }

    /**
     * Replacement effect (MTG Rule 614.1c): checks the controller's graveyard for cards with
     * {@link GraveyardEnterWithAdditionalCountersEffect} and adds +1/+1 counters to matching
     * creatures as they enter the battlefield. Uses CR 614.12 lookahead via
     * {@link GameQueryService#permanentWouldHaveSubtype} to determine subtypes.
     *
     * @param simultaneouslyEntered permanents to exclude from lookahead (see CR 614.12)
     */
    private void applyGraveyardEnterWithAdditionalCounters(GameData gameData, UUID controllerId,
                                                            Permanent permanent, List<Permanent> simultaneouslyEntered) {
        if (!permanent.getCard().hasType(CardType.CREATURE)) return;

        boolean cantHaveCounters = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof CantHaveCountersEffect);
        if (cantHaveCounters) return;

        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        if (graveyard == null || graveyard.isEmpty()) return;

        int additionalCounters = 0;
        for (Card card : graveyard) {
            for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GraveyardEnterWithAdditionalCountersEffect graveyardEffect) {
                    if (gameQueryService.permanentWouldHaveSubtype(gameData, permanent, controllerId,
                            simultaneouslyEntered, graveyardEffect.subtype())) {
                        additionalCounters += graveyardEffect.count();
                    }
                }
            }
        }

        if (additionalCounters > 0) {
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

        boolean cantHaveCounters = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof CantHaveCountersEffect);
        if (cantHaveCounters) return;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null || battlefield.isEmpty()) return;

        int additionalCounters = 0;
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ControlledCreaturesEnterWithAdditionalCountersEffect controlledEffect) {
                    if (gameQueryService.permanentWouldHaveSubtype(gameData, permanent, controllerId,
                            simultaneouslyEntered, controlledEffect.subtype())) {
                        additionalCounters += controlledEffect.count();
                    }
                }
            }
        }

        if (additionalCounters > 0) {
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + additionalCounters);
            log.info("Game {} - {} enters with {} additional +1/+1 counter(s) from battlefield static effect(s)",
                    gameData.id, permanent.getCard().getName(), additionalCounters);
        }
    }


    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, 0, false, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, false, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked) {
        handleCreatureEnteredBattlefield(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, List.of());
    }

    public void handleCreatureEnteredBattlefield(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        // Track kicked status on the permanent for "if wasn't kicked" end-step triggers (e.g. Skizzik)
        if (kicked) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEnteredPerm = bf.get(bf.size() - 1);
            justEnteredPerm.setKicked(true);
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

        boolean needsColorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(e -> e instanceof ChooseColorEffect);
        if (needsColorChoice) {
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            Permanent justEntered = bf.get(bf.size() - 1);
            playerInputService.beginColorChoice(gameData, controllerId, justEntered.getId(), targetId);
            return;
        }

        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand, etbMode, kicked, targetIds);
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

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId, boolean wasCastFromHand, int etbMode, boolean kicked, List<UUID> targetIds) {
        // Torpor Orb: "Creatures entering don't cause abilities to trigger."
        if (gameQueryService.areCreatureETBTriggersSuppressed(gameData, card)) {
            log.info("Game {} - {} ETB triggers suppressed (creature entering triggers disabled)", gameData.id, card.getName());
            return;
        }

        // Naban, Dean of Iteration: extra triggers when a Wizard enters
        int extraWizardTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, card);

        List<CardEffect> triggeredEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> !(e instanceof ChooseColorEffect))
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
            EtbEffectContext etbCtx = new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode, kicked, evoked, prowl);
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
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;
                gameData.queueMayAbility(card, controllerId, may, null, sourcePermanentId);
                // Naban: extra triggers for Wizard ETB
                for (int i = 0; i < extraWizardTriggers; i++) {
                    gameData.queueMayAbility(card, controllerId, may, null, sourcePermanentId);
                }
            }

            if (!mandatoryEffects.isEmpty()) {
                queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                        mandatoryEffects, modeTargetFilter, extraWizardTriggers);
            }
        }

        triggerCollectionService.checkAllyCreatureEntersTriggers(gameData, controllerId, card, extraWizardTriggers);
        triggerCollectionService.checkAllyArtifactEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAllyEquipmentEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAllyNontokenArtifactEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkOpponentCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAnyCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkEntersFromGraveyardTriggers(gameData, controllerId, card);
        triggerCollectionService.checkPermanentEntersFromGraveyardTriggers(gameData, controllerId, card);
        if (card.hasType(CardType.LAND)) {
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
        TargetCategory wrappedCategory = wrapped.targetSpec().category();
        if (!wrappedCategory.includesPermanents() || wrappedCategory.includesPlayers() || wrappedCategory.isGraveyard()) {
            return false;
        }
        if (!(card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter)) {
            return false;
        }
        FilterContext ctx = FilterContext.of(gameData)
                .withSourceCardId(card.getId())
                .withSourceControllerId(controllerId);
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (predicateEvaluationService.matchesPermanentPredicate(p, filter.predicate(), ctx)) {
                    return false;
                }
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
     * the stack (CR 603.3b). Naban doubling applies to every path via {@code extraWizardTriggers}.
     */
    private void queueMandatoryETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          List<UUID> targetIds, List<CardEffect> mandatoryEffects,
                                          TargetFilter modeTargetFilter, int extraWizardTriggers) {
        // Separate graveyard exile effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardExileEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileCardsFromGraveyardEffect).toList();
        // Separate graveyard cast effects (need single-target selection at trigger time)
        List<CardEffect> graveyardCastEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect).toList();
        // Separate graveyard flashback-grant effects (need single-target selection at trigger time)
        List<CardEffect> graveyardFlashbackEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof GrantFlashbackToTargetGraveyardCardEffect).toList();
        // Separate graveyard exile-and-may-play effects (need single-target selection at trigger time)
        List<CardEffect> graveyardMayPlayEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect).toList();
        // Separate opponent-graveyard steal effects (need single-target selection at trigger time)
        List<CardEffect> graveyardStealEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect).toList();
        // Separate graveyard return-to-hand effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardReturnToHandEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ReturnTargetCardsFromGraveyardToHandEffect).toList();
        List<CardEffect> otherEffects = mandatoryEffects.stream()
                .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect))
                .filter(e -> !(e instanceof CastTargetInstantOrSorceryFromGraveyardEffect))
                .filter(e -> !(e instanceof GrantFlashbackToTargetGraveyardCardEffect))
                .filter(e -> !(e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect))
                .filter(e -> !(e instanceof PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect))
                .filter(e -> !(e instanceof ReturnTargetCardsFromGraveyardToHandEffect))
                .filter(e -> !EffectResolution.targetsSpellOnStack(e)).toList();
        // Separate spell-targeting effects (need stack-target selection at trigger time)
        List<CardEffect> spellTargetEffects = mandatoryEffects.stream()
                .filter(EffectResolution::targetsSpellOnStack).toList();

        // Put non-special effects on the stack as before
        if (!otherEffects.isEmpty()) {
            boolean cardNeedsTarget = EffectResolution.needsTarget(card);
            boolean hasTarget = targetId != null || !targetIds.isEmpty();

            // A permanent that entered without a target chosen at cast time — a token copy,
            // or a creature put onto the battlefield from a graveyard via undying,
            // reanimation, etc. — must still choose targets for its mandatory ETB as the
            // ability is put on the stack (CR 603.3b). Cast spells with "up to" targets that
            // chose 0 targets are excluded; they passed through cast-time target selection.
            List<Permanent> enteredBf = gameData.playerBattlefields.get(controllerId);
            Permanent justEnteredPermanent = enteredBf != null && !enteredBf.isEmpty()
                    ? enteredBf.getLast() : null;
            boolean enteredFromGraveyard = justEnteredPermanent != null
                    && justEnteredPermanent.getEnteredFromGraveyardOwnerId() != null;
            boolean choosesTargetAtTriggerTime = card.isToken() || enteredFromGraveyard;

            // A surviving gate-conditional ETB (Metalcraft, Morbid, Raid, … — the gate was met
            // as the permanent entered) that targets never chose a target at cast time
            // (CR 601.2c): it is excluded from cast-time targeting by EffectResolution, so the
            // controller picks the target as the trigger goes on the stack (CR 603.3d), on the
            // same deferred path token copies and reanimated permanents use. A stale targetId
            // from the cast is deliberately ignored — the engine never asked for it.
            boolean gateConditionalNeedsTarget = otherEffects.stream()
                    .anyMatch(e -> e instanceof ConditionalEffect ce && ce.condition().isEtbTriggerGate()
                            && (ce.targetSpec().category().includesPlayers() || ce.targetSpec().category().includesPermanents()));

            if (gateConditionalNeedsTarget
                    || (cardNeedsTarget && !hasTarget && choosesTargetAtTriggerTime)) {
                // CR 603.3: no target was chosen at cast time — the ETB target is gated behind
                // an intervening-if, or the permanent wasn't cast (token copy, or returned from
                // a graveyard via undying / reanimation). The controller must choose a target
                // as the triggered ability is put on the stack.
                // For non-token casts with "up to N" abilities where 0 was chosen,
                // the ETB still triggers but has no effect — we skip queueing it.
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                if (card.getSpellTargets().size() > 1 || etbTokenTargetService.hasGroupWithMaxTargetsGreaterThanOne(card)) {
                    // Multi-target ETB (e.g. Burning Sun's Avatar, or a single group with
                    // "up to N" targets): choose slot-by-slot at trigger time,
                    // accumulating into targetIds.
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, List.of(), 0, 0));
                    for (int i = 0; i < extraWizardTriggers; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, List.of(), 0, 0));
                    }
                    String etbLog = card.getName() + "'s enter-the-battlefield ability triggers — choose targets.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(etbLog));
                    log.info("Game {} - {} ETB multi-target trigger queued (no target chosen at cast time)",
                            gameData.id, card.getName());
                } else {
                    TargetFilter etbTargetFilter = modeTargetFilter != null ? modeTargetFilter : card.getTargetFilter();

                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter));
                    for (int i = 0; i < extraWizardTriggers; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId, etbTargetFilter));
                    }
                    String etbLog = card.getName() + "'s enter-the-battlefield ability triggers — choose a target.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(etbLog));
                    log.info("Game {} - {} ETB trigger queued for target selection (no target chosen at cast time)",
                            gameData.id, card.getName());
                }
            } else if (!cardNeedsTarget || hasTarget) {
                List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
                UUID sourcePermanentId = bf != null && !bf.isEmpty() ? bf.getLast().getId() : null;

                StackEntry etbEntry = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        card,
                        controllerId,
                        card.getName() + "'s ETB ability",
                        new ArrayList<>(otherEffects),
                        0,
                        targetId,
                        sourcePermanentId,
                        Map.of(),
                        null,
                        List.of(),
                        targetIds != null ? targetIds : List.of()
                );
                if (modeTargetFilter != null) {
                    etbEntry.setTargetFilter(modeTargetFilter);
                }
                gameData.stack.add(etbEntry);
                String etbLog = card.getName() + "'s enter-the-battlefield ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(etbLog));
                log.info("Game {} - {} ETB ability pushed onto stack", gameData.id, card.getName());
                // Naban: extra triggers for Wizard ETB
                for (int i = 0; i < extraWizardTriggers; i++) {
                    StackEntry extraEtbEntry = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            card,
                            controllerId,
                            card.getName() + "'s ETB ability",
                            new ArrayList<>(otherEffects),
                            0,
                            targetId,
                            sourcePermanentId,
                            Map.of(),
                            null,
                            List.of(),
                            targetIds != null ? targetIds : List.of()
                    );
                    if (modeTargetFilter != null) {
                        extraEtbEntry.setTargetFilter(modeTargetFilter);
                    }
                    gameData.stack.add(extraEtbEntry);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(etbLog));
                    log.info("Game {} - {} ETB ability pushed onto stack (Wizard ETB extra trigger)", gameData.id, card.getName());
                }
            }
        }

        // Handle graveyard exile effects: targets must be chosen at trigger time
        for (CardEffect effect : graveyardExileEffects) {
            ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
            for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                graveyardTargetingService.handleGraveyardExileETBTargeting(gameData, controllerId, card, mandatoryEffects, exile);
            }
        }

        // Handle graveyard cast effects: target instant/sorcery in opponent's graveyard
        for (CardEffect effect : graveyardCastEffects) {
            for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                graveyardTargetingService.handleGraveyardCastETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard flashback-grant effects: target instant/sorcery in controller's graveyard
        for (CardEffect effect : graveyardFlashbackEffects) {
            for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                graveyardTargetingService.handleGrantFlashbackETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard exile-and-may-play effects: target card in controller's graveyard
        for (CardEffect effect : graveyardMayPlayEffects) {
            for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                graveyardTargetingService.handleGraveyardMayPlayETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle opponent-graveyard steal effects: target creature card in an opponent's graveyard
        for (CardEffect effect : graveyardStealEffects) {
            for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                graveyardTargetingService.handlePutCreatureFromOpponentGraveyardETBTargeting(gameData, controllerId, card, List.of(effect));
            }
        }

        // Handle graveyard return-to-hand effects: up to N target cards in controller's graveyard
        for (CardEffect effect : graveyardReturnToHandEffects) {
            for (int t = 0; t < 1 + extraWizardTriggers; t++) {
                graveyardTargetingService.handleReturnToHandETBTargeting(gameData, controllerId, card,
                        List.of(effect), (ReturnTargetCardsFromGraveyardToHandEffect) effect);
            }
        }

        // Handle spell-targeting ETB effects: target must be chosen from spells on the stack
        for (CardEffect effect : spellTargetEffects) {
            StackEntryPredicate spellFilter = null;
            if (effect instanceof CopySpellEffect cse) {
                spellFilter = cse.spellFilter();
            } else if (card.getTargetFilter() instanceof StackEntryPredicateTargetFilter sf) {
                // "counter target spell with mana value X or less" (Spellstutter Sprite): the
                // legal-spell restriction lives on the card's target filter, not the effect.
                spellFilter = sf.predicate();
            }
            gameData.queueInteraction(new PermanentChoiceContext.ETBSpellTargetTrigger(
                    card, controllerId, List.of(effect), spellFilter));
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
}
