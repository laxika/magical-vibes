package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.AggregateManaValueTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DistributeCountersAmongTargetsEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.OptionalTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.TargetSpec;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.GraveyardCardPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;

@Slf4j
@Component
public class ETBTokenTargetService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TargetLegalityService targetLegalityService;
    private final AmountEvaluationService amountEvaluationService;
    private final GraveyardTargetingService graveyardTargetingService;
    private final TriggerTargetCollector triggerTargetCollector;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    @Autowired
    private ObjectProvider<TriggerCollectionService> triggerCollectionService;

    @Autowired
    public ETBTokenTargetService(GameQueryService gameQueryService,
                                 PredicateEvaluationService predicateEvaluationService,
                                 GameLogService gameLogService,
                                 PlayerInputService playerInputService,
                                 TargetLegalityService targetLegalityService,
                                 AmountEvaluationService amountEvaluationService,
                                 GraveyardTargetingService graveyardTargetingService,
                                 TriggerTargetCollector triggerTargetCollector,
                                 InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.targetLegalityService = targetLegalityService;
        this.amountEvaluationService = amountEvaluationService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.triggerTargetCollector = triggerTargetCollector;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    public ETBTokenTargetService(GameQueryService gameQueryService,
                                 PredicateEvaluationService predicateEvaluationService,
                                 GameLogService gameLogService,
                                 PlayerInputService playerInputService,
                                 TargetLegalityService targetLegalityService) {
        this(gameQueryService, predicateEvaluationService, gameLogService, playerInputService,
                targetLegalityService, new AmountEvaluationService(predicateEvaluationService, gameQueryService), null,
                new TriggerTargetCollector(gameQueryService, predicateEvaluationService, targetLegalityService), null);
    }

    public ETBTokenTargetService(GameQueryService gameQueryService,
                                 PredicateEvaluationService predicateEvaluationService,
                                 GameLogService gameLogService,
                                 PlayerInputService playerInputService,
                                 TargetLegalityService targetLegalityService,
                                 GraveyardTargetingService graveyardTargetingService) {
        this(gameQueryService, predicateEvaluationService, gameLogService, playerInputService,
                targetLegalityService, new AmountEvaluationService(predicateEvaluationService, gameQueryService),
                graveyardTargetingService,
                new TriggerTargetCollector(gameQueryService, predicateEvaluationService, targetLegalityService), null);
    }

    public void processNextETBSpellTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class)) {
            PermanentChoiceContext.ETBSpellTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);

            List<UUID> validSpellCardIds = new ArrayList<>();
            for (StackEntry se : gameData.stack) {
                StackEntryType type = se.getEntryType();
                boolean isSpell = type == StackEntryType.INSTANT_SPELL || type == StackEntryType.SORCERY_SPELL
                        || type == StackEntryType.CREATURE_SPELL || type == StackEntryType.ENCHANTMENT_SPELL
                        || type == StackEntryType.ARTIFACT_SPELL || type == StackEntryType.PLANESWALKER_SPELL
                        || type == StackEntryType.BATTLE_SPELL;
                boolean isAbility = type == StackEntryType.ACTIVATED_ABILITY
                        || type == StackEntryType.TRIGGERED_ABILITY;
                if (!isSpell && !(pending.includeAbilities() && isAbility)) {
                    continue;
                }
                if (pending.spellFilter() != null
                        && !targetLegalityService.matchesStackEntryPredicate(gameData, se, pending.spellFilter(), pending.controllerId())) {
                    continue;
                }
                validSpellCardIds.add(se.getTargetableId());
            }

            List<UUID> validPermanentTargetIds = new ArrayList<>();
            boolean canTargetPermanents = pending.effects().stream()
                    .anyMatch(effect -> effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
            if (canTargetPermanents) {
                Permanent sourcePermanent = pending.sourcePermanentId() == null
                        ? null : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
                TriggerTargetCollector.Result permanentTargets = triggerTargetCollector.collect(
                        gameData, pending.effects(), pending.sourceCard().getTargetFilter(),
                        pending.controllerId(), pending.sourceCard(), TriggerTargetCollector.Options.ATTACK,
                        sourcePermanent);
                validPermanentTargetIds.addAll(permanentTargets.validTargets().stream()
                        .filter(id -> !gameData.playerIds.contains(id))
                        .toList());
            }

            if (validSpellCardIds.isEmpty() && validPermanentTargetIds.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);
                String targetType = canTargetPermanents ? "targets" : "spell targets";
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s enter-the-battlefield ability has no valid " + targetType + "."));
                log.info("Game {} - {} ETB spell-target trigger skipped (no valid targets)", gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            List<UUID> validAnyTargets = new ArrayList<>(validSpellCardIds);
            validAnyTargets.addAll(validPermanentTargetIds);
            String targetPrompt = canTargetPermanents
                    ? pending.sourceCard().getName() + "'s ability — Choose a target."
                    : pending.sourceCard().getName() + "'s ability — Choose target spell.";
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validAnyTargets, List.of(), targetPrompt);

            String targetDescription = canTargetPermanents ? "a target" : "a target spell";
            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                    "'s ETB ability triggers — choose " + targetDescription + "."));
            log.info("Game {} - {} ETB spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextETBTokenTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)) {
            PermanentChoiceContext.ETBTokenTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);

            TriggerTargetCollector.Result targets = collectTargets(gameData, pending);
            List<UUID> validSpellTargets = validMixedEtbSpellTargets(gameData, pending);
            List<UUID> validPlayerTargets = targets.validTargets().stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            List<UUID> validExiledCardTargets = targets.validTargets().stream()
                    .filter(id -> gameData.findExiledCard(id) != null)
                    .toList();
            List<UUID> validPermanentTargets = targets.validTargets().stream()
                    .filter(id -> !gameData.playerIds.contains(id))
                    .filter(id -> !validExiledCardTargets.contains(id))
                    .toList();
            List<UUID> validTargetObjects = new ArrayList<>(validPermanentTargets);
            validSpellTargets.stream()
                    .filter(id -> !validTargetObjects.contains(id))
                    .forEach(validTargetObjects::add);
            boolean optionalTarget = hasOptionalSingleTarget(pending);
            if (!validExiledCardTargets.isEmpty()
                    && validPlayerTargets.isEmpty()
                    && validSpellTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
                interactionHandlerRegistry.begin(gameData,
                        new PendingInteraction.ETBExiledCardTargetChoice(
                                pending.sourceCard(), pending.controllerId(), pending.effects(),
                                pending.sourcePermanentId(), validExiledCardTargets,
                                validPermanentTargets, pending.triggeringPermanentId()));
                log.info("Game {} - {} ETB exiled-card or permanent target trigger awaiting target selection",
                        gameData.id, pending.sourceCard().getName());
                return;
            }
            if (validPlayerTargets.isEmpty() && validTargetObjects.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
                if (optionalTarget) {
                    putTargetlessETBTriggerOnStack(gameData, pending);
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s enter-the-battlefield ability has no valid targets and resolves without a target."));
                    log.info("Game {} - {} ETB token-target trigger resolves without a target",
                            gameData.id, pending.sourceCard().getName());
                } else {
                    gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                            "'s enter-the-battlefield ability has no valid targets."));
                    log.info("Game {} - {} ETB token-target trigger skipped (no valid targets)",
                            gameData.id, pending.sourceCard().getName());
                }
                continue;
            }

            if (!validPermanentTargets.isEmpty() && validPlayerTargets.isEmpty() && validSpellTargets.isEmpty()
                    && pending.effects().stream().anyMatch(CardEffect::targetChosenAtRandom)) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
                UUID randomTarget = validPermanentTargets.get(
                        ThreadLocalRandom.current().nextInt(validPermanentTargets.size()));
                putTargetedETBTriggerOnStack(gameData, pending, randomTarget);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(),
                        "'s enter-the-battlefield ability randomly targets "
                                + gameQueryService.findPermanentById(gameData, randomTarget).getCard().getName() + "."));
                if (triggerCollectionService != null) {
                    triggerCollectionService.getObject().checkBecomesTargetOfAbilityTriggers(gameData);
                    triggerCollectionService.getObject().checkCrimeTriggers(gameData, gameData.stack.getLast());
                }
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
            if (optionalTarget && validPlayerTargets.contains(pending.controllerId())) {
                // The controller is a legal target, so "choose yourself" can't also mean "no target":
                // an empty answer chooses zero targets for the "up to one" target (CR 601.2c, CR 603.3d).
                playerInputService.beginMultiPermanentOrPlayerChoice(gameData, pending.controllerId(),
                        validTargetObjects, validPlayerTargets, 1,
                        new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.EtbOptionalTarget(pending),
                        pending.sourceCard().getName() + "'s ability — Choose up to one target, or choose none.");
                log.info("Game {} - {} ETB optional-target trigger awaiting target selection",
                        gameData.id, pending.sourceCard().getName());
                return;
            }
            gameData.interaction.setPermanentChoiceContext(pending);
            List<UUID> validPlayerChoiceTargets = new ArrayList<>(validPlayerTargets);
            boolean controllerDeclines = optionalTarget && !validPlayerChoiceTargets.contains(pending.controllerId());
            if (controllerDeclines) {
                validPlayerChoiceTargets.add(pending.controllerId());
            }
            String targetDescription = controllerDeclines
                    ? "Choose up to one target, or choose yourself to decline."
                    : validSpellTargets.isEmpty()
                            ? "Choose a target."
                            : "Choose a target creature or spell.";
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validTargetObjects, validPlayerChoiceTargets,
                    pending.sourceCard().getName() + "'s ability — " + targetDescription);

            log.info("Game {} - {} ETB token-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * True when choosing the controller in this single-target ETB prompt declines its optional
     * target, which is only the case when the controller is not itself a legal target.
     */
    public boolean controllerChoiceDeclines(GameData gameData, PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        return hasOptionalSingleTarget(pending)
                && !collectTargets(gameData, pending).validTargets().contains(pending.controllerId());
    }

    private TriggerTargetCollector.Result collectTargets(GameData gameData,
                                                         PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        return triggerTargetCollector.collect(
                gameData, pending.effects(), pending.targetFilter(), pending.controllerId(),
                pending.sourceCard(), TriggerTargetCollector.Options.ATTACK,
                pending.sourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId()),
                null, pending.xValue());
    }

    private static boolean hasOptionalSingleTarget(PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        if (pending.effects().stream().anyMatch(effect -> effect instanceof OptionalTargetEffect
                || effect.hasOptionalTarget())) {
            return true;
        }
        if (pending.sourceCard().getSpellTargets().size() != 1) {
            return false;
        }
        var declaredTarget = pending.sourceCard().getSpellTargets().getFirst();
        return declaredTarget.getMinTargets() == 0
                && declaredTarget.getMaxTargets() == 1
                && pending.effects().stream().anyMatch(effect ->
                pending.sourceCard().getEffectTargetIndex(effect) == declaredTarget.getIndex());
    }

    private void putTargetlessETBTriggerOnStack(GameData gameData,
                                                 PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        putTargetedETBTriggerOnStack(gameData, pending, null);
    }

    private void putTargetedETBTriggerOnStack(GameData gameData,
                                              PermanentChoiceContext.ETBTokenTargetTrigger pending,
                                              UUID targetId) {
        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                pending.sourceCard(),
                pending.controllerId(),
                pending.sourceCard().getName() + "'s ETB ability",
                new ArrayList<>(pending.effects()),
                targetId,
                pending.sourcePermanentId());
        entry.setXValue(pending.xValue());
        if (targetId != null && pending.targetFilter() != null) {
            entry.setTargetFilter(pending.targetFilter());
        }
        entry.setTriggeringPermanentId(pending.triggeringPermanentId());
        if (pending.sourcePermanentId() != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
            if (sourcePermanent != null) {
                entry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                entry.setSpectacle(sourcePermanent.isSpectacle());
                entry.setCollectEvidenceCostPaid(sourcePermanent.isCollectEvidenceCostPaid());
                entry.setWaterbendCostPaid(sourcePermanent.isWaterbendCostPaid());
                entry.setRevealCardFromHandCostPaid(sourcePermanent.isRevealCardFromHandCostPaid());
                entry.setControlledDragonAsCast(sourcePermanent.isControlledDragonAsCast());
                entry.setGiftPromised(sourcePermanent.isGiftPromised());
            }
        }
        gameData.stack.add(entry);
    }

    private List<UUID> validMixedEtbSpellTargets(GameData gameData,
                                                   PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        return validMixedEtbSpellTargets(gameData, pending.effects(), pending.controllerId());
    }

    private List<UUID> validMixedEtbSpellTargets(GameData gameData, List<CardEffect> effects,
                                                   UUID controllerId) {
        List<UUID> validTargets = new ArrayList<>();
        for (CardEffect effect : effects) {
            TargetSpec targetSpec = effect.targetSpec();
            if (!targetSpec.admits(TargetPredicate.Kind.SPELL)) {
                continue;
            }
            TargetPredicate.Spells spells = (TargetPredicate.Spells) targetSpec.declaredTarget()
                    .leaf(TargetPredicate.Kind.SPELL).orElseThrow();
            for (StackEntry stackEntry : gameData.stack) {
                if (isSpell(stackEntry)
                        && targetLegalityService.matchesStackEntryPredicate(
                        gameData, stackEntry, spells.inner(), controllerId)) {
                    validTargets.add(stackEntry.getTargetableId());
                }
            }
        }
        return validTargets;
    }

    private static boolean isSpell(StackEntry stackEntry) {
        StackEntryType type = stackEntry.getEntryType();
        return type == StackEntryType.INSTANT_SPELL || type == StackEntryType.SORCERY_SPELL
                || type == StackEntryType.CREATURE_SPELL || type == StackEntryType.ENCHANTMENT_SPELL
                || type == StackEntryType.ARTIFACT_SPELL || type == StackEntryType.PLANESWALKER_SPELL
                || type == StackEntryType.BATTLE_SPELL;
    }

    /**
     * Continues an ETB target choice whose effect also needs a multi-card graveyard selection.
     * Returns {@code true} when the ordinary stack-entry path must be skipped.
     */
    public boolean handleETBTokenTargetChosen(GameData gameData, UUID targetId,
            PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        GraveyardCardChoosingEffect choosingEffect = pending.effects().stream()
                .filter(e -> e instanceof GraveyardCardChoosingEffect candidate
                        && candidate.choosesGraveyardCards()
                        && e.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                .map(GraveyardCardChoosingEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (choosingEffect == null) {
            return false;
        }

        graveyardTargetingService.handleTargetPlayerGraveyardChoiceETBTargeting(
                gameData, pending.controllerId(), pending.sourceCard(), pending.effects(), targetId, choosingEffect);
        if (!gameData.interaction.isAwaitingInput()) {
            processNextETBTokenTargetTrigger(gameData);
        }
        return true;
    }

    public void processNextETBTokenMultiTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class)) {
            PermanentChoiceContext.ETBTokenMultiTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
            Card card = pending.sourceCard();
            List<SpellTarget> groups = card.getSpellTargets();
            int idx = pending.currentGroupIndex();
            int chosenInGroup = pending.chosenInCurrentGroup();

            if (idx >= groups.size()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                pushMultiTargetETBStackEntry(gameData, pending);
                continue;
            }

            SpellTarget group = groups.get(idx);
            int effectiveMinTargets = effectiveMinTargets(gameData, pending, group);
            int effectiveMaxTargets = effectiveMaxTargets(gameData, pending, group);

            if (chosenInGroup >= effectiveMaxTargets) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                        pending.chosenTargetsSoFar(), idx + 1, 0,
                        withGroupSize(pending.groupSizes(), chosenInGroup), pending.xValue(),
                        pending.repeatedAdditionalCosts(),
                        pending.resumePendingMayResolution(), pending.triggeringCardId(),
                        pending.triggeringPermanentId(), pending.eventValue(), pending.planarSource())
                        .withStateTriggerEffectIndex(pending.stateTriggerEffectIndex()));
                continue;
            }

            List<CardEffect> groupEffects = effectsForTargetGroup(card, pending.effects(), group.getIndex());

            // A target group whose bound effect was gated out (its intervening-if wasn't met as the
            // permanent entered, e.g. Noggle Hedge-Mage's Islands / Mountains ETBs) has no surviving
            // effect to target — advance past it without demanding targets, so a still-active later
            // group can still be chosen and the ability isn't wrongly skipped (CR 603.4).
            if (groupEffects.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                        pending.chosenTargetsSoFar(), idx + 1, 0,
                        withGroupSize(pending.groupSizes(), chosenInGroup), pending.xValue(),
                        pending.repeatedAdditionalCosts(),
                        pending.resumePendingMayResolution(), pending.triggeringCardId(),
                        pending.triggeringPermanentId(), pending.eventValue(), pending.planarSource())
                        .withStateTriggerEffectIndex(pending.stateTriggerEffectIndex()));
                continue;
            }

            TargetFilter groupFilter = group.getFilter();
            boolean filterRestrictsTargetKind = filterRestrictsTargetKind(groupFilter);
            boolean canTargetPermanent = groupEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    || groupFilter instanceof PermanentPredicateTargetFilter
                    || groupFilter instanceof ControlledPermanentPredicateTargetFilter
                    || groupFilter instanceof OwnedPermanentPredicateTargetFilter;
            boolean canTargetExiledCard = groupEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD));
            boolean canTargetGraveyardCard = groupFilter instanceof GraveyardCardPredicateTargetFilter
                    || (!filterRestrictsTargetKind && groupEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)));

            List<UUID> validPlayerTargets = playerTargets(gameData, pending, group, groupEffects);

            List<UUID> validPermanentTargets = new ArrayList<>();
            if (canTargetPermanent) {
                // One shared layered pass for the whole candidate scan: every per-candidate
                // legality query reads the same board instead of re-fingerprinting it.
                validPermanentTargets.addAll(gameQueryService.withQueryScope(gameData, () -> {
                    List<UUID> candidates = new ArrayList<>();
                    for (UUID pid : gameData.orderedPlayerIds) {
                        List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                        if (battlefield == null) continue;
                        for (Permanent p : battlefield) {
                            if (targetAlreadyChosen(pending, p.getId())) continue;
                            if (matchesPermanentTargetFilter(gameData, p, group.getFilter(),
                                    pending.controllerId(), card, pending.sourcePermanentId(), pending.xValue())
                                    // Shroud, hexproof and protection still apply (Gilded Drake vs Morphling).
                                    && targetLegalityService.checkTriggeredPermanentTargetableReason(
                                            gameData, p, card, pending.controllerId()).isEmpty()) {
                                candidates.add(p.getId());
                            }
                        }
                    }
                    return candidates;
                }));
            }

            List<UUID> validExiledCardTargets = new ArrayList<>();
            if (canTargetExiledCard) {
                TriggerTargetCollector.Result exiledTargets = triggerTargetCollector.collect(
                        gameData, groupEffects, groupFilter, pending.controllerId(), card,
                        TriggerTargetCollector.Options.ATTACK,
                        pending.sourcePermanentId() == null
                                ? null
                                : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId()));
                validExiledCardTargets.addAll(exiledTargets.validTargets().stream()
                        .filter(id -> gameData.findExiledCard(id) != null)
                        .filter(id -> !targetAlreadyChosen(pending, id))
                        .toList());
            }

            List<UUID> validGraveyardCardTargets = validGraveyardCardTargets(
                    gameData, card, pending, groupFilter, canTargetGraveyardCard, groupEffects);

            List<UUID> validSpellTargets = validMixedEtbSpellTargets(
                    gameData, groupEffects, pending.controllerId());
            validSpellTargets.stream()
                    .filter(id -> !validPermanentTargets.contains(id))
                    .forEach(validPermanentTargets::add);

            if (card.getMultiTargetConstraint() == MultiTargetConstraint.SHARE_CARD_TYPE
                    && !pending.chosenTargetsSoFar().isEmpty()) {
                List<Permanent> selected = pending.chosenTargetsSoFar().stream()
                        .map(id -> gameQueryService.findPermanentById(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                validPermanentTargets.removeIf(id -> {
                    Permanent candidate = gameQueryService.findPermanentById(gameData, id);
                    return candidate != null && selected.stream()
                            .noneMatch(first -> gameQueryService.sharesCardType(gameData, first, candidate));
                });
            }

            if (isOnePerControllerConstraint(card.getMultiTargetConstraint())
                    && !pending.chosenTargetsSoFar().isEmpty()) {
                List<UUID> selectedControllers = pending.chosenTargetsSoFar().stream()
                        .map(id -> targetControllerId(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                validPermanentTargets.removeIf(id ->
                        selectedControllers.contains(gameQueryService.findPermanentController(gameData, id)));
                validGraveyardCardTargets.removeIf(id ->
                        selectedControllers.contains(gameQueryService.findGraveyardOwnerById(gameData, id)));
            }

            if (card.getMultiTargetConstraint() == MultiTargetConstraint.CONTROLLED_BY_FIRST_TARGET
                    && !pending.chosenTargetsSoFar().isEmpty()) {
                UUID firstTargetId = pending.chosenTargetsSoFar().getFirst();
                UUID requiredControllerId = gameData.playerIds.contains(firstTargetId)
                        ? firstTargetId
                        : gameQueryService.findPermanentController(gameData, firstTargetId);
                if (requiredControllerId == null && !gameData.playerIds.contains(firstTargetId)) {
                    requiredControllerId = gameQueryService.findGraveyardOwnerById(gameData, firstTargetId);
                }
                UUID controllerId = requiredControllerId;
                validPermanentTargets.removeIf(id ->
                        !java.util.Objects.equals(controllerId,
                                gameQueryService.findPermanentController(gameData, id)));
            }

            if (card.getMultiTargetConstraint()
                    == MultiTargetConstraint.CONTROLLED_BY_PLAYER_DAMAGED_BY_FIRST_TARGET_THIS_COMBAT
                    && !pending.chosenTargetsSoFar().isEmpty()) {
                UUID firstTargetId = pending.chosenTargetsSoFar().getFirst();
                Set<UUID> damagedPlayerIds = gameData.combatDamageToPlayersThisCombat
                        .getOrDefault(firstTargetId, Set.of());
                validPermanentTargets.removeIf(id ->
                        !damagedPlayerIds.contains(gameQueryService.findPermanentController(gameData, id)));
            }

            int aggregateManaValueLimit = aggregateManaValueLimit(gameData, pending, groupEffects);
            if (aggregateManaValueLimit >= 0) {
                int selectedManaValue = currentGroupTargetIds(pending).stream()
                        .map(id -> gameQueryService.findPermanentById(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .mapToInt(permanent -> permanent.getCard().getManaValue())
                        .sum();
                validPermanentTargets.removeIf(id -> {
                    Permanent candidate = gameQueryService.findPermanentById(gameData, id);
                    return candidate != null
                            && selectedManaValue + candidate.getCard().getManaValue() > aggregateManaValueLimit;
                });
            }

            boolean noLegalTargets = validPlayerTargets.isEmpty()
                    && validPermanentTargets.isEmpty()
                    && validExiledCardTargets.isEmpty()
                    && validGraveyardCardTargets.isEmpty();

            int availableTargetCount = validPlayerTargets.size() + validPermanentTargets.size()
                    + validExiledCardTargets.size() + validGraveyardCardTargets.size();
            boolean cannotReachMinimum = chosenInGroup < effectiveMinTargets
                    && availableTargetCount < effectiveMinTargets - chosenInGroup;

            if (noLegalTargets || cannotReachMinimum) {
                if (chosenInGroup < effectiveMinTargets) {
                    gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                    gameLogService.append(gameData, GameLog.cardThen(card, "'s enter-the-battlefield ability has no valid targets."));
                    log.info("Game {} - {} ETB multi-target trigger skipped (no valid targets for mandatory group {} at slot {})",
                            gameData.id, card.getName(), idx, chosenInGroup);
                    continue;
                }
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenMultiTargetTrigger.class);
                gameData.queueInteractionFirst(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                        pending.chosenTargetsSoFar(), idx + 1, 0,
                        withGroupSize(pending.groupSizes(), chosenInGroup), pending.xValue(),
                        pending.repeatedAdditionalCosts(),
                        pending.resumePendingMayResolution(), pending.triggeringCardId(),
                        pending.triggeringPermanentId(), pending.eventValue(), pending.planarSource())
                        .withStateTriggerEffectIndex(pending.stateTriggerEffectIndex()));
                continue;
            }

            boolean minMet = chosenInGroup >= effectiveMinTargets;
            // An optional group that can target the controller can't use "choose yourself" to stop,
            // so it asks for the whole group at once and an empty answer chooses no targets for it
            // (CR 601.2c, CR 603.3d) — even a single-slot "up to one target player" group, and a
            // group that may also target graveyard or exiled cards.
            if (effectiveMinTargets == 0
                    && validPlayerTargets.contains(pending.controllerId())) {
                List<UUID> validCardTargets = new ArrayList<>(validExiledCardTargets);
                validCardTargets.addAll(validGraveyardCardTargets);
                playerInputService.beginMultiPermanentOrPlayerChoice(gameData, pending.controllerId(),
                        validPermanentTargets, validPlayerTargets, validCardTargets,
                        Math.min(validPermanentTargets.size() + validPlayerTargets.size() + validCardTargets.size(),
                                effectiveMaxTargets - chosenInGroup),
                        new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext.EtbPlayerTargetGroup(pending),
                        card.getName() + "'s ability — Choose targets, or choose none.");
                return;
            }
            boolean mustChooseRemainingController =
                    card.getMultiTargetConstraint() == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE;
            boolean controllerStops = minMet && !mustChooseRemainingController
                    && !validPlayerTargets.contains(pending.controllerId());
            if (controllerStops) {
                validPlayerTargets.add(pending.controllerId());
            }

            boolean optionalGraveyardTargetCanBeDeclined = effectiveMinTargets == 0
                    && validPlayerTargets.size() == 1
                    && validPlayerTargets.contains(pending.controllerId());
            if (!validGraveyardCardTargets.isEmpty()
                    && validPermanentTargets.isEmpty()
                    && validExiledCardTargets.isEmpty()
                    && (validPlayerTargets.isEmpty() || optionalGraveyardTargetCanBeDeclined)) {
                playerInputService.beginMultiPermanentChoice(
                        gameData, pending.controllerId(), List.of(), validGraveyardCardTargets,
                        Math.min(validGraveyardCardTargets.size(), effectiveMaxTargets - chosenInGroup),
                        new com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext
                                .EtbGraveyardCardTargetGroup(pending),
                        card.getName() + "'s ability — Choose target creature card from an opponent's graveyard.");
                return;
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            String slotLabel = "target " + (idx + 1)
                    + (group.getMaxTargets() > 1 ? "." + (chosenInGroup + 1) : "");
            String prompt = controllerStops
                    ? card.getName() + "'s ability — Choose " + slotLabel + " (or yourself to stop)."
                    : card.getName() + "'s ability — Choose " + slotLabel + ".";
            validPermanentTargets.addAll(validExiledCardTargets);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            log.info("Game {} - {} ETB multi-target trigger awaiting target (group {} slot {})",
                    gameData.id, card.getName(), idx, chosenInGroup);
            return;
        }
    }

    /**
     * True when choosing the controller in this multi-target ETB prompt stops adding targets to the
     * current group rather than targeting the controller, which is only the case when the controller
     * is not itself a legal target for that group.
     */
    public boolean controllerChoiceStopsGroup(GameData gameData,
                                              PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        List<SpellTarget> groups = pending.sourceCard().getSpellTargets();
        if (pending.currentGroupIndex() >= groups.size()) {
            return true;
        }
        SpellTarget group = groups.get(pending.currentGroupIndex());
        List<CardEffect> groupEffects = effectsForTargetGroup(pending.sourceCard(), pending.effects(), group.getIndex());
        return !playerTargets(gameData, pending, group, groupEffects).contains(pending.controllerId());
    }

    private List<UUID> playerTargets(GameData gameData, PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                     SpellTarget group, List<CardEffect> groupEffects) {
        TargetFilter groupFilter = group.getFilter();
        boolean canTargetPlayer = groupFilter instanceof PlayerPredicateTargetFilter
                || (!filterRestrictsTargetKind(groupFilter) && groupEffects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER)));
        List<UUID> validPlayerTargets = new ArrayList<>();
        if (!canTargetPlayer) {
            return validPlayerTargets;
        }
        for (UUID pid : gameData.orderedPlayerIds) {
            if (targetAlreadyChosen(pending, pid)) continue;
            if (targetLegalityService.isPlayerUntargetable(
                    gameData, pid, pending.controllerId(), pending.sourceCard())) continue;
            if (matchesPlayerTargetFilter(gameData, pending.controllerId(), pid, groupFilter)) {
                validPlayerTargets.add(pid);
            }
        }
        if (isOnePerControllerConstraint(pending.sourceCard().getMultiTargetConstraint())
                && !pending.chosenTargetsSoFar().isEmpty()) {
            List<UUID> selectedControllers = pending.chosenTargetsSoFar().stream()
                    .map(id -> targetControllerId(gameData, id))
                    .filter(java.util.Objects::nonNull)
                    .toList();
            validPlayerTargets.removeIf(selectedControllers::contains);
        }
        return validPlayerTargets;
    }

    private static boolean filterRestrictsTargetKind(TargetFilter groupFilter) {
        return groupFilter instanceof PlayerPredicateTargetFilter
                || groupFilter instanceof PermanentPredicateTargetFilter
                || groupFilter instanceof ControlledPermanentPredicateTargetFilter
                || groupFilter instanceof OwnedPermanentPredicateTargetFilter
                || groupFilter instanceof GraveyardCardPredicateTargetFilter;
    }

    private List<UUID> validGraveyardCardTargets(
            GameData gameData,
            Card card,
            PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
            TargetFilter targetFilter,
            boolean canTargetGraveyardCard,
            List<CardEffect> groupEffects) {
        if (!canTargetGraveyardCard
                || !(targetFilter instanceof GraveyardCardPredicateTargetFilter graveyardFilter)
                || !gameQueryService.canGraveyardCardsBeTargeted(gameData)) {
            return new ArrayList<>();
        }

        Set<UUID> selectedControllers = pending.chosenTargetsSoFar().stream()
                .map(id -> targetControllerId(gameData, id))
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        List<UUID> validIds = new ArrayList<>();
        for (UUID graveyardOwnerId : graveyardFilter.scope()
                .graveyardOwners(gameData.orderedPlayerIds, pending.controllerId())) {
            if (isOnePerControllerConstraint(card.getMultiTargetConstraint())
                    && selectedControllers.contains(graveyardOwnerId)) {
                continue;
            }
            for (Card graveyardCard : gameData.playerGraveyards
                    .getOrDefault(graveyardOwnerId, List.of())) {
                if (targetAlreadyChosen(pending, graveyardCard.getId())
                        || gameQueryService.isLandCardTargetRestricted(
                        gameData, graveyardCard, pending.controllerId())
                        || targetLegalityService.checkTriggeredGraveyardTarget(
                        gameData, card, groupEffects, graveyardCard.getId(), pending.controllerId(),
                        pending.sourcePermanentId(), pending.xValue()).isPresent()) {
                    continue;
                }
                if (graveyardFilter.predicate() == null
                        || predicateEvaluationService.matchesCardPredicate(
                        graveyardCard, graveyardFilter.predicate(), card.getId(), gameData,
                        graveyardOwnerId, pending.sourcePermanentId(), null, pending.xValue())) {
                    validIds.add(graveyardCard.getId());
                }
            }
        }
        return validIds;
    }

    private UUID targetControllerId(GameData gameData, UUID targetId) {
        if (gameData.playerIds.contains(targetId)) {
            return targetId;
        }
        UUID permanentController = gameQueryService.findPermanentController(gameData, targetId);
        return permanentController != null
                ? permanentController
                : gameQueryService.findGraveyardOwnerById(gameData, targetId);
    }

    private List<UUID> graveyardTargetsForEntry(
            Card card, PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        List<UUID> chosenTargets = pending.chosenTargetsSoFar();
        List<UUID> graveyardTargets = new ArrayList<>();
        int offset = 0;
        List<SpellTarget> groups = card.getSpellTargets();
        for (int i = 0; i < groups.size() && offset < chosenTargets.size(); i++) {
            SpellTarget group = groups.get(i);
            int groupSize = i < pending.groupSizes().size()
                    ? pending.groupSizes().get(i)
                    : Math.min(group.getMaxTargets(), chosenTargets.size() - offset);
            int end = Math.min(chosenTargets.size(), offset + Math.max(groupSize, 0));
            if (group.getFilter() instanceof GraveyardCardPredicateTargetFilter) {
                graveyardTargets.addAll(chosenTargets.subList(offset, end));
            }
            offset = end;
        }
        return graveyardTargets;
    }

    private boolean targetAlreadyChosen(PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                        UUID candidateId) {
        List<UUID> chosenTargets = pending.chosenTargetsSoFar();
        int currentGroupStart = pending.groupSizes().stream()
                .mapToInt(Integer::intValue)
                .sum();
        boolean chosenInCurrentGroup = chosenTargets.subList(currentGroupStart, chosenTargets.size())
                .contains(candidateId);
        return chosenInCurrentGroup
                || (!pending.sourceCard().isAllowSharedTargets() && chosenTargets.contains(candidateId));
    }

    private List<UUID> currentGroupTargetIds(PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        int currentGroupStart = pending.groupSizes().stream().mapToInt(Integer::intValue).sum();
        return pending.chosenTargetsSoFar().subList(currentGroupStart, pending.chosenTargetsSoFar().size());
    }

    private int aggregateManaValueLimit(GameData gameData,
                                        PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                        List<CardEffect> effects) {
        return effects.stream()
                .filter(effect -> effect instanceof AggregateManaValueTargetEffect aggregateEffect
                        && aggregateEffect.hasAggregateManaValueLimit()
                        && effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .map(AggregateManaValueTargetEffect.class::cast)
                .mapToInt(effect -> {
                    if (effect.dynamicMaxTotalManaValue() == null) {
                        return effect.maxTotalManaValue();
                    }
                    Permanent source = pending.sourcePermanentId() == null
                            ? null
                            : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
                    return amountEvaluationService.evaluate(gameData, effect.dynamicMaxTotalManaValue(),
                            new AmountContext(pending.controllerId(), source, null, pending.xValue(),
                                    pending.eventValue(), false, null, pending.repeatedAdditionalCosts(),
                                    pending.sourceCard()));
                })
                .min()
                .orElse(-1);
    }

    private boolean isOnePerControllerConstraint(MultiTargetConstraint constraint) {
        return constraint == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                || constraint == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE;
    }

    private int effectiveMaxTargets(GameData gameData,
                                    PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                    SpellTarget group) {
        Permanent enteringSource = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        int maxTargets = enteringSource != null && enteringSource.isKicked()
                ? group.getKickedMaxTargets() : group.getMaxTargets();
        int staticMax = group.isXScaled() ? Math.min(pending.xValue(), maxTargets) : maxTargets;
        if (group.getDynamicMaxTargets() == null) {
            return staticMax;
        }
        Permanent source = pending.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        int dynamicMax = amountEvaluationService.evaluate(gameData, group.getDynamicMaxTargets(),
                new AmountContext(pending.controllerId(), source, null, pending.xValue(), pending.eventValue(), false,
                        null, pending.repeatedAdditionalCosts(), null));
        return Math.min(staticMax, Math.max(0, dynamicMax));
    }

    public int effectiveMinTargets(GameData gameData,
                                   PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        SpellTarget group = pending.sourceCard().getSpellTargets().get(pending.currentGroupIndex());
        return effectiveMinTargets(gameData, pending, group);
    }

    private int effectiveMinTargets(GameData gameData,
                                    PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                    SpellTarget group) {
        Permanent enteringSource = gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        int minTargets = enteringSource != null && enteringSource.isKicked()
                ? group.getKickedMinTargets() : group.getMinTargets();
        int staticMin = group.isXScaled() ? Math.min(pending.xValue(), minTargets) : minTargets;
        if (group.getDynamicMinTargets() == null) {
            return staticMin;
        }
        Permanent source = pending.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        int dynamicMin = amountEvaluationService.evaluate(gameData, group.getDynamicMinTargets(),
                new AmountContext(pending.controllerId(), source, null, pending.xValue(), pending.eventValue(), false,
                        null, pending.repeatedAdditionalCosts(), null));
        return Math.max(staticMin, Math.max(0, dynamicMin));
    }

    /**
     * The targets a counter distribution divides its counters among: only those chosen for the target
     * group the effect is bound to, so another group's target (Rhino, Terrible Trampler's destroyed
     * land) is never offered counters. An effect not bound to a group divides among every target.
     */
    private List<UUID> distributionTargetIds(PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                             DistributeCountersAmongTargetsEffect distribution) {
        List<UUID> chosen = pending.chosenTargetsSoFar();
        int group = pending.sourceCard().getEffectTargetIndex(distribution);
        List<Integer> sizes = pending.groupSizes();
        if (group < 0 || group >= sizes.size() || sizes.size() != pending.sourceCard().getSpellTargets().size()) {
            return chosen;
        }
        int start = 0;
        for (int i = 0; i < group; i++) {
            start += sizes.get(i);
        }
        int end = Math.min(chosen.size(), start + sizes.get(group));
        return start > end ? List.of() : List.copyOf(chosen.subList(start, end));
    }

    private void pushMultiTargetETBStackEntry(GameData gameData,
                                               PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        if (pending.resumePendingMayResolution()) {
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry != null) {
                pendingEntry.setDeclaredTargetIds(pending.chosenTargetsSoFar());
                pendingEntry.setTargetGroupSizes(List.copyOf(pending.groupSizes()));
            }
            return;
        }
        Card card = pending.sourceCard();
        DistributeCountersAmongTargetsEffect counterDistribution =
                findChosenCounterDistribution(pending.effects());
        Map<UUID, Integer> counterAssignments = Map.of();
        List<UUID> distributionTargets = counterDistribution == null
                ? List.of() : distributionTargetIds(pending, counterDistribution);
        if (counterDistribution != null && !distributionTargets.isEmpty()) {
            Permanent source = pending.sourcePermanentId() == null
                    ? null : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
            int total = amountEvaluationService.evaluate(gameData, counterDistribution.total(),
                    new AmountContext(pending.controllerId(), source, null, pending.xValue(),
                            pending.eventValue(), false, null, pending.repeatedAdditionalCosts(), null));
            Map<UUID, Integer> presetAssignments = gameData.pendingETBDamageAssignments;
            boolean validPreset = presetAssignments != null
                    && !presetAssignments.isEmpty()
                    && distributionTargets.containsAll(presetAssignments.keySet())
                    && presetAssignments.values().stream().allMatch(amount -> amount != null && amount > 0)
                    && (counterDistribution.allowsPartialDistribution()
                    ? presetAssignments.values().stream().mapToInt(Integer::intValue).sum() <= total
                    : presetAssignments.values().stream().mapToInt(Integer::intValue).sum() == total);
            if (total <= 0) {
                counterAssignments = Map.of();
            } else if (validPreset) {
                counterAssignments = Map.copyOf(presetAssignments);
                gameData.pendingETBDamageAssignments = Map.of();
            } else {
                playerInputService.beginCounterDistributionAssignmentChoice(
                        gameData,
                        pending.controllerId(),
                        new ChoiceContext.CounterDistributionAssignment(
                                card, pending.controllerId(), pending.effects(), pending.sourcePermanentId(),
                                counterDistribution.counterType(), distributionTargets, Map.of(),
                                total, 0, counterDistribution.allowsPartialDistribution(), null, null,
                                pending.chosenTargetsSoFar(), pending.groupSizes()));
                return;
            }
        }
        com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect dividedDamage = pending.effects().stream()
                .filter(effect -> effect instanceof com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect divided
                        && divided.etbAssignments())
                .map(effect -> (com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect) effect)
                .findFirst().orElse(null);
        if (dividedDamage != null && !pending.chosenTargetsSoFar().isEmpty()) {
            Permanent source = pending.sourcePermanentId() == null ? null
                    : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
            int total = amountEvaluationService.evaluate(gameData, dividedDamage.totalDamage(),
                    new AmountContext(pending.controllerId(), source, null, pending.xValue(),
                            pending.eventValue(), false, null, pending.repeatedAdditionalCosts(), null));
            playerInputService.beginCounterDistributionAssignmentChoice(gameData, pending.controllerId(),
                    new ChoiceContext.CounterDistributionAssignment(card, pending.controllerId(),
                            pending.effects(), pending.sourcePermanentId(), null,
                            pending.chosenTargetsSoFar(), Map.of(), total, 0));
            return;
        }
        // Shared by ETB token copies, ON_SELF_CAST, and multi-target ON_ATTACK — keep the label generic.
        String abilityLabel = card.getName() + "'s ability";
        Zone targetZone = pending.chosenTargetsSoFar().stream()
                .map(id -> gameQueryService.findStackEntryByCardId(gameData, id))
                .anyMatch(stackEntry -> stackEntry != null && isSpell(stackEntry))
                ? Zone.STACK : null;
        StackEntry etbEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                pending.controllerId(),
                abilityLabel,
                new ArrayList<>(pending.effects()),
                pending.xValue(),
                null,
                pending.sourcePermanentId(),
                counterAssignments,
                targetZone,
                graveyardTargetsForEntry(card, pending),
                new ArrayList<>(pending.chosenTargetsSoFar())
        );
        etbEntry.setMultiTargetConstraint(card.getMultiTargetConstraint());
        etbEntry.setTargetGroupSizes(List.copyOf(pending.groupSizes()));
        etbEntry.setEventValue(pending.eventValue());
        etbEntry.setTriggeringCardId(pending.triggeringCardId());
        if (pending.stateTriggerEffectIndex() >= 0) {
            etbEntry.setStateTriggerEffectIndex(pending.stateTriggerEffectIndex());
        }
        if (pending.planarSource() != null) {
            etbEntry.setSourcePlanarObject(pending.planarSource().copy());
        }
        if (pending.triggeringPermanentId() != null && card.isAura()) {
            etbEntry.setTargetId(pending.triggeringPermanentId());
        }
        if (pending.sourcePermanentId() != null) {
            etbEntry.setTriggeringPermanentId(pending.triggeringPermanentId() != null
                    ? pending.triggeringPermanentId() : pending.sourcePermanentId());
            Permanent sourcePermanent = gameQueryService.findPermanentById(
                    gameData, pending.sourcePermanentId());
            if (sourcePermanent != null) {
                etbEntry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
                etbEntry.setAttackedTargetId(sourcePermanent.getAttackTarget());
                etbEntry.setGiftPromised(sourcePermanent.isGiftPromised());
                // "If it was kicked/bargained" conditions on the trigger read the permanent's cast payment.
                etbEntry.setKicked(sourcePermanent.isKicked());
                if (card.isAura() && sourcePermanent.isAttached() && sourcePermanent.getAttachedTo() != null) {
                    Permanent enchanted = gameQueryService.findPermanentById(gameData, sourcePermanent.getAttachedTo());
                    if (enchanted != null) {
                        etbEntry.setAttachedPermanentSnapshot(new Permanent(enchanted));
                        etbEntry.setTriggeringPermanentId(enchanted.getId());
                        etbEntry.setTriggeringPermanentControllerId(
                                gameQueryService.findPermanentController(gameData, enchanted.getId()));
                        etbEntry.setTriggeringPermanentPowerAtTrigger(
                                gameQueryService.getEffectivePower(gameData, enchanted));
                    }
                }
                // An evoked permanent's independent ETB abilities and its evoke sacrifice trigger
                // together, so its controller may order them (CR 603.3b).
                if (pending.triggeringCardId() == null && sourcePermanent.isEvoked()
                        && card.getEffectRegistrations(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                        .anyMatch(registration -> registration.triggerMode() == TriggerMode.INDEPENDENT)) {
                    etbEntry.setTriggeringCardId(card.getId());
                }
            }
        }
        if (!pending.repeatedAdditionalCosts().isEmpty()) {
            etbEntry.setRepeatedAdditionalCosts(List.copyOf(pending.repeatedAdditionalCosts()));
        }
        gameData.stack.add(etbEntry);
        if (triggerCollectionService != null) {
            triggerCollectionService.getObject().checkCrimeTriggers(gameData, etbEntry);
        }
        gameLogService.append(gameData, GameLog.cardThen(card, "'s ability triggers."));
        log.info("Game {} - {} multi-target ability pushed onto stack ({} targets)",
                gameData.id, card.getName(), pending.chosenTargetsSoFar().size());
    }

    private DistributeCountersAmongTargetsEffect findChosenCounterDistribution(List<CardEffect> effects) {
        for (CardEffect effect : effects) {
            if (effect instanceof DistributeCountersAmongTargetsEffect distribution
                    && distribution.mode() == DivisionMode.CHOSEN) {
                return distribution;
            }
            if (effect instanceof ConditionalEffect conditional) {
                DistributeCountersAmongTargetsEffect nested =
                        findChosenCounterDistribution(List.of(conditional.wrapped()));
                if (nested != null) {
                    return nested;
                }
            }
        }
        return null;
    }

    /**
     * Appends the number of targets a just-finished group actually took to the running per-group
     * tally, so a declined "up to N" group is recorded as the 0 it was rather than being assumed to
     * have consumed its full {@code maxTargets} when the flat target list is sliced back apart.
     *
     * @param sizes  the tally for the groups finished so far
     * @param size   how many targets the group that just finished took
     * @return a new immutable tally with {@code size} appended
     */
    private static List<Integer> withGroupSize(List<Integer> sizes, int size) {
        List<Integer> updated = new ArrayList<>(sizes);
        updated.add(size);
        return List.copyOf(updated);
    }

    /**
     * True when the card declares more than one target group, so its targets must be walked group by
     * group (each group has its own filter and its own bound effects) even when no single group takes
     * more than one target — Boros Battleshaper's two independent "up to one target creature" groups.
     *
     * @param card the source card whose target groups to inspect
     * @return whether slot-by-slot walking is required for multiple groups
     */
    public boolean hasMultipleTargetGroups(Card card) {
        return card.getSpellTargets().size() > 1;
    }

    public boolean hasGroupWithMaxTargetsGreaterThanOne(Card card) {
        return card.getSpellTargets().stream().anyMatch(g -> g.getMaxTargets() > 1);
    }

    /**
     * True when ETB / self-cast trigger targeting must walk targets slot-by-slot: either a group
     * allows more than one target, or a group is optional ({@code minTargets == 0}, "up to N")
     * so the controller may decline via choosing themselves.
     */
    public boolean needsSlotBySlotTargetSelection(Card card) {
        return card.getSpellTargets().stream()
                .anyMatch(g -> g.getMaxTargets() > 1 || g.getMinTargets() == 0
                        || g.getDynamicMinTargets() != null);
    }

    private List<CardEffect> effectsForTargetGroup(Card card, List<CardEffect> effects, int groupIndex) {
        // A bare positional group — one no effect is bound to — is read by index by an unbound effect
        // (Goblin Grenadiers' DestroyEachTargetPermanentEffect over "target creature and target land").
        // Such a group must still be targeted; only a group whose bound effect was gated out below
        // may be skipped.
        if (!card.bindsEffectToTargetGroup(groupIndex)) {
            return effects;
        }
        List<CardEffect> matched = new ArrayList<>();
        for (CardEffect effect : effects) {
            if (card.isEffectBoundToTargetGroup(effect, groupIndex)) {
                matched.add(effect);
            }
        }
        return matched;
    }

    private boolean matchesPlayerTargetFilter(GameData gameData, UUID controllerId, UUID candidatePlayerId,
                                              TargetFilter targetFilter) {
        if (targetFilter instanceof AnyTargetPredicateTargetFilter anyFilter) {
            return targetLegalityService.matchesPlayerPredicate(
                    gameData, controllerId, candidatePlayerId, anyFilter.playerPredicate());
        }
        if (!(targetFilter instanceof PlayerPredicateTargetFilter playerFilter)) {
            return true;
        }
        return targetLegalityService.matchesPlayerPredicate(
                gameData, controllerId, candidatePlayerId, playerFilter.predicate());
    }

    private boolean matchesPermanentTargetFilter(GameData gameData, Permanent permanent,
                                                  TargetFilter targetFilter,
                                                  UUID controllerId, Card sourceCard,
                                                  UUID sourcePermanentId, int xValue) {
        if (targetFilter == null) {
            return gameQueryService.isCreature(gameData, permanent);
        }
        if (targetFilter instanceof PlayerPredicateTargetFilter
                || targetFilter instanceof StackEntryPredicateTargetFilter) {
            return false;
        }
        FilterContext filterContext = new FilterContext(
                gameData, sourceCard.getId(), controllerId, xValue, null, sourcePermanentId);
        return predicateEvaluationService.checkTargetFilter(targetFilter, permanent, filterContext).isEmpty();
    }
}
