package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.OwnedPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import com.github.laxika.magicalvibes.service.trigger.TriggerTargetCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;

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

    @Autowired
    public ETBTokenTargetService(GameQueryService gameQueryService,
                                 PredicateEvaluationService predicateEvaluationService,
                                 GameLogService gameLogService,
                                 PlayerInputService playerInputService,
                                 TargetLegalityService targetLegalityService,
                                 AmountEvaluationService amountEvaluationService,
                                 GraveyardTargetingService graveyardTargetingService,
                                 TriggerTargetCollector triggerTargetCollector) {
        this.gameQueryService = gameQueryService;
        this.predicateEvaluationService = predicateEvaluationService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.targetLegalityService = targetLegalityService;
        this.amountEvaluationService = amountEvaluationService;
        this.graveyardTargetingService = graveyardTargetingService;
        this.triggerTargetCollector = triggerTargetCollector;
    }

    public ETBTokenTargetService(GameQueryService gameQueryService,
                                 PredicateEvaluationService predicateEvaluationService,
                                 GameLogService gameLogService,
                                 PlayerInputService playerInputService,
                                 TargetLegalityService targetLegalityService) {
        this(gameQueryService, predicateEvaluationService, gameLogService, playerInputService,
                targetLegalityService, new AmountEvaluationService(predicateEvaluationService, gameQueryService), null,
                new TriggerTargetCollector(gameQueryService, predicateEvaluationService, targetLegalityService));
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
                new TriggerTargetCollector(gameQueryService, predicateEvaluationService, targetLegalityService));
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
                validSpellCardIds.add(se.getCard().getId());
            }

            if (validSpellCardIds.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(), "'s enter-the-battlefield ability has no valid spell targets."));
                log.info("Game {} - {} ETB spell-target trigger skipped (no valid targets)", gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ETBSpellTargetTrigger.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validSpellCardIds, List.of(),
                    pending.sourceCard().getName() + "'s ability — Choose target spell.");

            gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(), "'s ETB ability triggers — choose a target spell."));
            log.info("Game {} - {} ETB spell-target trigger awaiting target selection", gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    public void processNextETBTokenTargetTrigger(GameData gameData) {
        while (gameData.hasPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class)) {
            PermanentChoiceContext.ETBTokenTargetTrigger pending = gameData.peekPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);

            TriggerTargetCollector.Result targets = triggerTargetCollector.collect(
                    gameData, pending.effects(), pending.targetFilter(), pending.controllerId(),
                    pending.sourceCard(), TriggerTargetCollector.Options.ATTACK,
                    pending.triggeringPermanentId() == null
                            ? null
                            : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId()));
            List<UUID> validPlayerTargets = targets.validTargets().stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            List<UUID> validPermanentTargets = targets.validTargets().stream()
                    .filter(id -> !gameData.playerIds.contains(id))
                    .toList();
            if (validPlayerTargets.isEmpty() && validPermanentTargets.isEmpty()) {
                gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
                gameLogService.append(gameData, GameLog.cardThen(pending.sourceCard(), "'s enter-the-battlefield ability has no valid targets."));
                log.info("Game {} - {} ETB token-target trigger skipped (no valid targets)",
                        gameData.id, pending.sourceCard().getName());
                continue;
            }

            gameData.pollPendingInteraction(PermanentChoiceContext.ETBTokenTargetTrigger.class);
            gameData.interaction.setPermanentChoiceContext(pending);
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets,
                    pending.sourceCard().getName() + "'s ability — Choose a target.");

            log.info("Game {} - {} ETB token-target trigger awaiting target selection",
                    gameData.id, pending.sourceCard().getName());
            return;
        }
    }

    /**
     * Continues an ETB target choice whose effect also needs a multi-card graveyard selection.
     * Returns {@code true} when the ordinary stack-entry path must be skipped.
     */
    public boolean handleETBTokenTargetChosen(GameData gameData, UUID targetId,
            PermanentChoiceContext.ETBTokenTargetTrigger pending) {
        GraveyardCardChoosingEffect choosingEffect = pending.effects().stream()
                .filter(e -> e instanceof GraveyardCardChoosingEffect
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
                        pending.resumePendingMayResolution()));
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
                        pending.resumePendingMayResolution()));
                continue;
            }

            TargetFilter groupFilter = group.getFilter();
            boolean canTargetPlayer = groupEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PLAYER))
                    || groupFilter instanceof PlayerPredicateTargetFilter;
            boolean canTargetPermanent = groupEffects.stream()
                    .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                    || groupFilter instanceof PermanentPredicateTargetFilter
                    || groupFilter instanceof ControlledPermanentPredicateTargetFilter
                    || groupFilter instanceof OwnedPermanentPredicateTargetFilter;

            List<UUID> validPlayerTargets = new ArrayList<>();
            if (canTargetPlayer) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    if (pending.chosenTargetsSoFar().contains(pid)) continue;
                    if (matchesPlayerTargetFilter(gameData, pending.controllerId(), pid, group.getFilter())) {
                        validPlayerTargets.add(pid);
                    }
                }
            }

            List<UUID> validPermanentTargets = new ArrayList<>();
            if (canTargetPermanent) {
                for (UUID pid : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                    if (battlefield == null) continue;
                    for (Permanent p : battlefield) {
                        if (pending.chosenTargetsSoFar().contains(p.getId())) continue;
                        if (matchesPermanentTargetFilter(gameData, p, group.getFilter(),
                                pending.controllerId(), card, pending.sourcePermanentId())) {
                            validPermanentTargets.add(p.getId());
                        }
                    }
                }
            }

            if (card.getMultiTargetConstraint() == MultiTargetConstraint.SHARE_CARD_TYPE
                    && !pending.chosenTargetsSoFar().isEmpty()) {
                List<Permanent> selected = pending.chosenTargetsSoFar().stream()
                        .map(id -> gameQueryService.findPermanentById(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                validPermanentTargets.removeIf(id -> {
                    Permanent candidate = gameQueryService.findPermanentById(gameData, id);
                    return candidate != null && selected.stream()
                            .noneMatch(first -> gameQueryService.sharesCardType(first, candidate));
                });
            }

            if (isOnePerControllerConstraint(card.getMultiTargetConstraint())
                    && !pending.chosenTargetsSoFar().isEmpty()) {
                List<UUID> selectedControllers = pending.chosenTargetsSoFar().stream()
                        .map(id -> gameQueryService.findPermanentController(gameData, id))
                        .filter(java.util.Objects::nonNull)
                        .toList();
                validPermanentTargets.removeIf(id ->
                        selectedControllers.contains(gameQueryService.findPermanentController(gameData, id)));
            }

            boolean noLegalTargets = validPlayerTargets.isEmpty() && validPermanentTargets.isEmpty();

            if (noLegalTargets) {
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
                        pending.resumePendingMayResolution()));
                continue;
            }

            boolean minMet = chosenInGroup >= effectiveMinTargets;
            boolean mustChooseRemainingController =
                    card.getMultiTargetConstraint() == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE;
            if (minMet && !mustChooseRemainingController
                    && !validPlayerTargets.contains(pending.controllerId())) {
                validPlayerTargets.add(pending.controllerId());
            }

            gameData.interaction.setPermanentChoiceContext(pending);
            String slotLabel = "target " + (idx + 1)
                    + (group.getMaxTargets() > 1 ? "." + (chosenInGroup + 1) : "");
            String prompt = minMet
                    ? card.getName() + "'s ability — Choose " + slotLabel + " (or yourself to stop)."
                    : card.getName() + "'s ability — Choose " + slotLabel + ".";
            playerInputService.beginAnyTargetChoice(gameData, pending.controllerId(),
                    validPermanentTargets, validPlayerTargets, prompt);

            log.info("Game {} - {} ETB multi-target trigger awaiting target (group {} slot {})",
                    gameData.id, card.getName(), idx, chosenInGroup);
            return;
        }
    }

    private boolean isOnePerControllerConstraint(MultiTargetConstraint constraint) {
        return constraint == MultiTargetConstraint.AT_MOST_ONE_PER_CONTROLLER
                || constraint == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE;
    }

    private int effectiveMaxTargets(GameData gameData,
                                    PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                    SpellTarget group) {
        if (group.getDynamicMaxTargets() == null) {
            return group.getMaxTargets();
        }
        Permanent source = pending.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        int dynamicMax = amountEvaluationService.evaluate(gameData, group.getDynamicMaxTargets(),
                new AmountContext(pending.controllerId(), source, null, pending.xValue(), 0, false,
                        null, pending.repeatedAdditionalCosts(), null));
        return Math.min(group.getMaxTargets(), Math.max(0, dynamicMax));
    }

    public int effectiveMinTargets(GameData gameData,
                                   PermanentChoiceContext.ETBTokenMultiTargetTrigger pending) {
        SpellTarget group = pending.sourceCard().getSpellTargets().get(pending.currentGroupIndex());
        return effectiveMinTargets(gameData, pending, group);
    }

    private int effectiveMinTargets(GameData gameData,
                                    PermanentChoiceContext.ETBTokenMultiTargetTrigger pending,
                                    SpellTarget group) {
        int staticMin = group.isXScaled()
                ? Math.min(pending.xValue(), group.getMinTargets())
                : group.getMinTargets();
        if (group.getDynamicMinTargets() == null) {
            return staticMin;
        }
        Permanent source = pending.sourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, pending.sourcePermanentId());
        int dynamicMin = amountEvaluationService.evaluate(gameData, group.getDynamicMinTargets(),
                new AmountContext(pending.controllerId(), source, null, pending.xValue(), 0, false,
                        null, pending.repeatedAdditionalCosts(), null));
        return Math.max(staticMin, Math.max(0, dynamicMin));
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
        // Shared by ETB token copies, ON_SELF_CAST, and multi-target ON_ATTACK — keep the label generic.
        String abilityLabel = card.getName() + "'s ability";
        StackEntry etbEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                pending.controllerId(),
                abilityLabel,
                new ArrayList<>(pending.effects()),
                pending.xValue(),
                null,
                pending.sourcePermanentId(),
                Map.of(),
                null,
                List.of(),
                new ArrayList<>(pending.chosenTargetsSoFar())
        );
        etbEntry.setTargetGroupSizes(List.copyOf(pending.groupSizes()));
        if (pending.sourcePermanentId() != null) {
            etbEntry.setTriggeringPermanentId(pending.sourcePermanentId());
            Permanent sourcePermanent = gameQueryService.findPermanentById(
                    gameData, pending.sourcePermanentId());
            if (sourcePermanent != null) {
                etbEntry.setSourcePermanentSnapshot(new Permanent(sourcePermanent));
            }
        }
        if (!pending.repeatedAdditionalCosts().isEmpty()) {
            etbEntry.setRepeatedAdditionalCosts(List.copyOf(pending.repeatedAdditionalCosts()));
        }
        gameData.stack.add(etbEntry);
        gameLogService.append(gameData, GameLog.cardThen(card, "'s ability triggers."));
        log.info("Game {} - {} multi-target ability pushed onto stack ({} targets)",
                gameData.id, card.getName(), pending.chosenTargetsSoFar().size());
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
            if (card.getEffectTargetIndex(effect) == groupIndex) {
                matched.add(effect);
            }
        }
        return matched;
    }

    private boolean matchesPlayerTargetFilter(GameData gameData, UUID controllerId, UUID candidatePlayerId,
                                              TargetFilter targetFilter) {
        if (!(targetFilter instanceof PlayerPredicateTargetFilter playerFilter)) {
            return true;
        }
        return targetLegalityService.matchesPlayerPredicate(
                gameData, controllerId, candidatePlayerId, playerFilter.predicate());
    }

    private boolean matchesPermanentTargetFilter(GameData gameData, Permanent permanent,
                                                  TargetFilter targetFilter,
                                                  UUID controllerId, Card sourceCard,
                                                  UUID sourcePermanentId) {
        if (targetFilter == null) {
            return gameQueryService.isCreature(gameData, permanent);
        }
        if (targetFilter instanceof PlayerPredicateTargetFilter) {
            return false;
        }
        FilterContext filterContext = new FilterContext(
                gameData, sourceCard.getId(), controllerId, null, null, sourcePermanentId);
        return predicateEvaluationService.checkTargetFilter(targetFilter, permanent, filterContext).isEmpty();
    }
}
