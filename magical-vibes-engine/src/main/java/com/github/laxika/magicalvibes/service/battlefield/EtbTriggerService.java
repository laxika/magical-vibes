package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseColorEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseBasicLandTypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseModeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneAtTriggerTimeEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePrimalClayFormOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseSubtypeOnEnterEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopySpellEffect;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.effect.TriggeredAbilityCounterEffect;
import com.github.laxika.magicalvibes.model.effect.BattlefieldAndGraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardsFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileGraveyardCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantFlashbackToTargetGraveyardCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardFromOpponentGraveyardOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromOpponentGraveyardOntoBattlefieldWithExileEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ReplacementEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectContext;
import com.github.laxika.magicalvibes.service.battlefield.etb.EtbEffectResolver;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Component
public class EtbTriggerService {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final EtbEffectResolver etbEffectResolver;
    private final AmountEvaluationService amountEvaluationService;
    private final PredicateEvaluationService predicateEvaluationService;

    public EtbTriggerService(GameQueryService gameQueryService,
                             GameLogService gameLogService,
                             PlayerInputService playerInputService,
                             @Lazy TriggerCollectionService triggerCollectionService,
                             GraveyardTargetingService graveyardTargetingService,
                             ETBTokenTargetService etbTokenTargetService,
                             EtbEffectResolver etbEffectResolver,
                             AmountEvaluationService amountEvaluationService,
                             PredicateEvaluationService predicateEvaluationService) {
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.etbEffectResolver = etbEffectResolver;
        this.amountEvaluationService = amountEvaluationService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId, int count) {
        triggerCollectionService.checkAllyTokenEntersTriggers(gameData, controllerId, count);
    }

    public void checkAllyTokenEntersTriggers(GameData gameData, UUID controllerId,
                                              List<UUID> permanentIds) {
        triggerCollectionService.checkAllyTokenEntersTriggers(gameData, controllerId, permanentIds);
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
        ChooseModeOnEnterEffect modeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseModeOnEnterEffect.class::isInstance)
                .map(ChooseModeOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && modeChoice != null
                && enteringPermanent.getChosenModeLabels().stream().noneMatch(modeChoice.modes()::contains)) {
            playerInputService.beginChooseModeOnEnterChoice(gameData, controllerId, card,
                    enteringPermanent.getId(), modeChoice.modes());
            return;
        }
        ChooseColorEffect colorChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(e -> e instanceof ChooseColorEffect)
                .map(e -> (ChooseColorEffect) e)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && enteringPermanent.getChosenColor() == null && colorChoice != null) {
            playerInputService.beginColorChoice(gameData, controllerId, enteringPermanent.getId(), null, colorChoice);
            return;
        }

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
        ChooseBasicLandTypeOnEnterEffect basicLandTypeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseBasicLandTypeOnEnterEffect.class::isInstance)
                .map(ChooseBasicLandTypeOnEnterEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (enteringPermanent != null && enteringPermanent.getChosenSubtype() == null
                && basicLandTypeChoice != null) {
            playerInputService.beginBasicLandTypeChoice(gameData, controllerId, enteringPermanent.getId(),
                    false, basicLandTypeChoice.choicesRequired() > 1, basicLandTypeChoice.allowedTypes());
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
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds,
                                          List<String> repeatedAdditionalCosts) {
        processCreatureETBEffects(gameData, controllerId, card, targetId, wasCastFromHand,
                etbMode, xValue, kicked, targetIds, repeatedAdditionalCosts, List.of());
    }

    public void processCreatureETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          boolean wasCastFromHand, int etbMode, int xValue,
                                          boolean kicked, List<UUID> targetIds,
                                          List<String> repeatedAdditionalCosts,
                                          List<UUID> convokeCreatureIds) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        Permanent enteringPermanent = battlefield != null && !battlefield.isEmpty() ? battlefield.getLast() : null;
        ChooseModeOnEnterEffect modeChoice = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .filter(ChooseModeOnEnterEffect.class::isInstance)
                .map(ChooseModeOnEnterEffect.class::cast)
                .findFirst().orElse(null);
        if (enteringPermanent != null && modeChoice != null
                && enteringPermanent.getChosenModeLabels().stream().noneMatch(modeChoice.modes()::contains)) {
            playerInputService.beginChooseModeOnEnterChoice(gameData, controllerId, card,
                    enteringPermanent.getId(), modeChoice.modes());
            return;
        }
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

        boolean enteringPermanentTriggersSuppressed = gameQueryService
                .areOpponentPermanentETBTriggersSuppressed(gameData, controllerId);
        int extraEtbTriggers = gameQueryService.countETBExtraTriggers(gameData, controllerId, controllerId, card);

        List<CardEffect> triggeredEffects = enteringPermanentTriggersSuppressed
                ? new ArrayList<>()
                : new ArrayList<>(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD));
        if (!enteringPermanentTriggersSuppressed && enteringPermanent != null) {
            triggeredEffects.addAll(enteringPermanent.getTemporaryTriggeredEffects(EffectSlot.ON_ENTER_BATTLEFIELD));
        }
        int additionalElementalTriggers = enteringPermanent == null ? 0
                : gameQueryService.countAdditionalTriggeredAbilityTriggers(
                        gameData, controllerId, enteringPermanent);
        int extraTriggerCopies = extraEtbTriggers + additionalElementalTriggers;
        if (!enteringPermanentTriggersSuppressed && battlefield != null) {
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
                .filter(e -> !(e instanceof ChooseBasicLandTypeOnEnterEffect))
                // Primal Clay's shape choice is made while the new permanent enters, not as an ETB ability.
                .filter(e -> !(e instanceof ChoosePrimalClayFormOnEnterEffect))
                // "As enters, choose a creature type" is a replacement-style choice made during entry
                // (handled via beginSubtypeChoice), not a triggered ability queued onto the stack.
                .filter(e -> !(e instanceof ChooseSubtypeOnEnterEffect))
                .filter(e -> !(e instanceof ChooseModeOnEnterEffect))
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
            List<ChooseOneAtTriggerTimeEffect> triggerTimeChoices = triggeredEffects.stream()
                    .filter(ChooseOneAtTriggerTimeEffect.class::isInstance)
                    .map(ChooseOneAtTriggerTimeEffect.class::cast)
                    .toList();
            // Evoke sacrifice gate (CR 603.4): read the just-entered permanent's evoked flag, which
            // was stamped from the spell's cast context at resolution time.
            List<Permanent> evokeBf = gameData.playerBattlefields.get(controllerId);
            boolean evoked = evokeBf != null && !evokeBf.isEmpty() && evokeBf.getLast().isEvoked();
            // Prowl gate (CR 603.4): read the just-entered permanent's prowl flag, stamped from the
            // spell's cast context at resolution time.
            boolean prowl = evokeBf != null && !evokeBf.isEmpty() && evokeBf.getLast().isProwl();
            boolean alternateCost = enteringPermanent != null && enteringPermanent.isAlternateCost();
            // Resolve each mandatory effect into its trigger-time form: modal unwrap, value
            // materialisation, and intervening-if gating (CR 603.4) — a null result drops the trigger.
            EtbEffectContext etbCtx = new EtbEffectContext(gameData, card, controllerId, wasCastFromHand, etbMode,
                    kicked, evoked, prowl, alternateCost, enteringPermanent, repeatedAdditionalCosts);
            List<CardEffect> mandatoryEffects = triggeredEffects.stream()
                    .filter(e -> !(e instanceof MayEffect))
                    .filter(e -> !(e instanceof ChooseOneAtTriggerTimeEffect))
                    .map(e -> e instanceof ChooseOneEffect chooseOne && chooseOne.choicesRequired() > 1
                            ? e : etbEffectResolver.resolve(etbCtx, e))
                    .filter(Objects::nonNull)
                    .toList();

            UUID triggerSourcePermanentId = enteringPermanent != null ? enteringPermanent.getId() : null;
            for (ChooseOneAtTriggerTimeEffect triggerTimeChoice : triggerTimeChoices) {
                for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                    gameData.queueInteraction(new PermanentChoiceContext.TriggeredModalTrigger(
                            card, controllerId, triggerTimeChoice.choice(), triggerSourcePermanentId));
                }
            }

            for (CardEffect effect : mayEffects) {
                MayEffect may = (MayEffect) effect;
                if (may.wrapped() instanceof SacrificePermanentThenEffect
                        && may.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    for (int i = 0; i < 1 + extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                                card, controllerId, List.of(may), xValue));
                    }
                    continue;
                }
                // CR 603.3d: a "may [do X to] target permanent" ETB (e.g. Leonin Relic-Warder)
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
                        || mayTargetSpec.admits(TargetPredicate.Kind.SPELL)
                        || mayTargetSpec.admits(TargetPredicate.Kind.GRAVEYARD_CARD)) {
                    queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                            List.of(may), modeTargetFilter, extraTriggerCopies, etbMode, xValue,
                            repeatedAdditionalCosts, convokeCreatureIds);
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

            if (gameData.hasPendingInteraction(PermanentChoiceContext.SpellGraveyardTargetTrigger.class)
                    && !gameData.interaction.isAwaitingInput()) {
                triggerCollectionService.processNextSpellGraveyardTargetTrigger(gameData);
            }

            if (!mandatoryEffects.isEmpty()) {
                queueMandatoryETBEffects(gameData, controllerId, card, targetId, targetIds,
                        mandatoryEffects, modeTargetFilter, extraTriggerCopies, etbMode, xValue,
                        repeatedAdditionalCosts, convokeCreatureIds);
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
        triggerCollectionService.checkCreatureEntersThisTurnTriggers(gameData, controllerId, card);
        triggerCollectionService.checkAnyPermanentEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkEnchantedPlayerCreatureEntersTriggers(gameData, controllerId, card);
        triggerCollectionService.checkEntersFromGraveyardTriggers(gameData, controllerId, card);
        triggerCollectionService.checkPermanentEntersFromGraveyardTriggers(gameData, controllerId, card);
        triggerCollectionService.checkSelfEntersFromGraveyardTriggers(gameData, controllerId, card);
        triggerCollectionService.checkGraveyardCreatureEntersFromGraveyardTriggers(gameData, controllerId, card);
        if (!faceDown && card.hasType(CardType.LAND)) {
            triggerCollectionService.checkOpponentLandEntersTriggers(gameData, controllerId, card);
            triggerCollectionService.checkAllyLandEntersTriggers(gameData, controllerId, card);
        }
    }

    /**
     * True when a "may" ETB ability targets a permanent (and only a permanent) via a concrete
     * predicate filter but no permanent on the battlefield satisfies it — meaning the targeted
     * triggered ability has no legal target and must not be put onto the stack (CR 603.3d).
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
     * the stack (CR 603.3d). Trigger-copy effects apply to every path via {@code extraTriggerCopies}.
     */
    private void queueMandatoryETBEffects(GameData gameData, UUID controllerId, Card card, UUID targetId,
                                          List<UUID> targetIds, List<CardEffect> mandatoryEffects,
                                          TargetFilter modeTargetFilter, int extraTriggerCopies,
                                          int etbMode, int xValue,
                                          List<String> repeatedAdditionalCosts,
                                          List<UUID> convokeCreatureIds) {
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
        // Separate graveyard return-to-battlefield effects (need multi-target selection at trigger time)
        List<CardEffect> graveyardReturnToBattlefieldEffects = mandatoryEffects.stream()
                .filter(e -> e instanceof ReturnTargetCardsFromGraveyardToBattlefieldEffect).toList();
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
                .filter(e -> !graveyardReturnToBattlefieldEffects.contains(e))
                .filter(e -> !graveyardShuffleIntoLibraryEffects.contains(e))
                .toList();
        List<CardEffect> otherEffects = mandatoryEffects.stream()
                .filter(e -> !(e instanceof ExileCardsFromGraveyardEffect))
                .filter(e -> !graveyardCastEffects.contains(e))
                .filter(e -> !(e instanceof GrantFlashbackToTargetGraveyardCardEffect))
                .filter(e -> !(e instanceof ExileTargetCardFromGraveyardMayPlayUntilNextTurnEffect))
                .filter(e -> !graveyardStealEffects.contains(e))
                .filter(e -> !(e instanceof ReturnTargetCardsFromGraveyardToHandEffect))
                .filter(e -> !graveyardReturnToBattlefieldEffects.contains(e))
                .filter(e -> !targetPlayerGraveyardChoiceEffects.contains(e))
                .filter(e -> !(e instanceof ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect))
                .filter(e -> !graveyardTargetReturnEffects.contains(e))
                .filter(e -> !graveyardCardsExileEffects.contains(e))
                .filter(e -> !mixedZoneChoiceEffects.contains(e))
                .filter(e -> !EffectResolution.targetsSpellOnStack(e)).toList();
        // Separate spell-targeting effects (need stack-target selection at trigger time)
        List<CardEffect> spellTargetEffects = mandatoryEffects.stream()
                .filter(EffectResolution::targetsSpellOnStack).toList();

        List<Permanent> sourceBattlefield = gameData.playerBattlefields.get(controllerId);
        boolean sourceWasCastForSpectacle = sourceBattlefield != null
                && !sourceBattlefield.isEmpty()
                && sourceBattlefield.getLast().isSpectacle();

        // Put non-special effects on the stack as before
        if (!otherEffects.isEmpty()) {
            List<UUID> activeTargetIds = targetsForActiveEtbGroups(card, otherEffects, targetIds);
            boolean hasTarget = targetId != null || !activeTargetIds.isEmpty();

            // A permanent that entered without a target chosen at cast time — a token copy,
            // a creature put onto the battlefield from a graveyard via undying / reanimation,
            // or a land (lands are played, never cast, so they never went through cast-time
            // target selection; e.g. Sunscorched Desert's "deals 1 damage to target player or
            // planeswalker") — must still choose targets for its mandatory ETB as the ability is
            // put on the stack (CR 603.3d). Cast spells with "up to" targets that chose 0 targets
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
                            && (!card.isAura() || card.getEffectTargetIndex(e) != 0)
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
                    || hasUnselectedDynamicEtbTargetGroup(card, otherEffects, targetId, targetIds)
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
                    List<UUID> initialTargets = card.isAura() || targetId == null
                            ? List.of() : List.of(targetId);
                    int initialGroupIndex = card.isAura() ? 1 : targetId == null ? 0 : 1;
                    List<Integer> initialGroupSizes = card.isAura()
                            ? List.of(0) : targetId == null ? List.of() : List.of(1);
                    gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                            card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                            initialTargets, initialGroupIndex, 0,
                            initialGroupSizes, xValue,
                            repeatedAdditionalCosts == null ? List.of() : List.copyOf(repeatedAdditionalCosts)));
                    for (int i = 0; i < extraTriggerCopies; i++) {
                        gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                                card, controllerId, new ArrayList<>(otherEffects), sourcePermanentId,
                                initialTargets, initialGroupIndex, 0,
                                initialGroupSizes, xValue,
                                repeatedAdditionalCosts == null ? List.of() : List.copyOf(repeatedAdditionalCosts)));
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
                if (repeatedAdditionalCosts != null && !repeatedAdditionalCosts.isEmpty()) {
                    etbEntry.setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
                }
                etbEntry.setConvokeCreatureIds(convokeCreatureIds);
                if (modeTargetFilter != null) {
                    etbEntry.setTargetFilter(modeTargetFilter);
                }
                Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                if (sourcePermanent != null) {
                    etbEntry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                }
                etbEntry.setSpectacle(sourceWasCastForSpectacle);
                gameData.stack.add(etbEntry);
                queueTriggeredAbilityCounters(gameData, etbEntry);
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
                    if (repeatedAdditionalCosts != null && !repeatedAdditionalCosts.isEmpty()) {
                        extraEtbEntry.setRepeatedAdditionalCosts(List.copyOf(repeatedAdditionalCosts));
                    }
                    extraEtbEntry.setConvokeCreatureIds(convokeCreatureIds);
                    if (modeTargetFilter != null) {
                        extraEtbEntry.setTargetFilter(modeTargetFilter);
                    }
                    if (sourcePermanent != null) {
                        extraEtbEntry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                    }
                    extraEtbEntry.setSpectacle(sourceWasCastForSpectacle);
                    gameData.stack.add(extraEtbEntry);
                    queueTriggeredAbilityCounters(gameData, extraEtbEntry);
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
        List<Permanent> enteredBattlefield = gameData.playerBattlefields.get(controllerId);
        UUID graveyardSourcePermanentId = enteredBattlefield == null || enteredBattlefield.isEmpty()
                ? null : enteredBattlefield.getLast().getId();
        for (CardEffect effect : graveyardExileEffects) {
            ExileCardsFromGraveyardEffect exile = (ExileCardsFromGraveyardEffect) effect;
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleGraveyardExileETBTargeting(
                        gameData, controllerId, card, mandatoryEffects, graveyardSourcePermanentId, exile);
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

        // Handle graveyard return-to-battlefield effects: up to the cast-context cap of target
        // creature cards from the controller's graveyard.
        for (CardEffect effect : graveyardReturnToBattlefieldEffects) {
            ReturnTargetCardsFromGraveyardToBattlefieldEffect returnEffect =
                    (ReturnTargetCardsFromGraveyardToBattlefieldEffect) effect;
            int maxTargets = returnEffect.dynamicMaxTargets() == null
                    ? returnEffect.maxTargets()
                    : Math.max(0, amountEvaluationService.evaluate(gameData, returnEffect.dynamicMaxTargets(),
                            new AmountContext(controllerId, null, null, xValue, 0, false, null,
                                    repeatedAdditionalCosts == null ? List.of() : repeatedAdditionalCosts, card)));
            for (int t = 0; t < 1 + extraTriggerCopies; t++) {
                graveyardTargetingService.handleReturnToBattlefieldETBTargeting(gameData, controllerId, card,
                        List.of(effect), returnEffect, maxTargets);
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
                        card, controllerId, List.of(effect), null, minimumGraveyardTargets, xValue));
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

    private void queueTriggeredAbilityCounters(GameData gameData, StackEntry triggeredAbility) {
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof TriggeredAbilityCounterEffect counterEffect)) {
                        continue;
                    }
                    StackEntry counterTrigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            permanent.getCard(),
                            controllerId,
                            permanent.getCard().getName() + "'s ability",
                            List.of(new CounterUnlessPaysEffect(counterEffect.counterCost())),
                            triggeredAbility.getCard().getId(),
                            Zone.STACK,
                            permanent.getId());
                    gameData.stack.add(counterTrigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(permanent.getCard()));
                }
            }
        });
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

    private boolean hasUnselectedDynamicEtbTargetGroup(Card card, List<CardEffect> effects,
                                                         UUID targetId, List<UUID> targetIds) {
        if (targetId == null || targetIds == null || !targetIds.isEmpty()) {
            return false;
        }
        for (SpellTarget group : card.getSpellTargets()) {
            if (group.getDynamicMaxTargets() == null) {
                continue;
            }
            boolean hasActiveTargetEffect = effects.stream()
                    .filter(effect -> card.getEffectTargetIndex(effect) == group.getIndex())
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                            || effect.targetSpec().admits(TargetPredicate.Kind.PLAYER));
            if (hasActiveTargetEffect) {
                return true;
            }
        }
        return false;
    }
}
