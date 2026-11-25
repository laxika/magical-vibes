package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;

import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.ValidTargetService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Announces the mode of modal triggered abilities that trigger sources put straight onto the stack
 * as a bare {@link ChooseOneEffect} (CR 603.3c, 700.2b). Before a player receives priority, the
 * controller chooses one mode among those that can legally be chosen and then that mode's target;
 * an ability with no legal mode is removed from the stack. The stack entry itself keeps its trigger
 * context (damaged player, event value, source snapshot), only its effects and target change.
 *
 * <p>Only plain "choose one" modals whose modes take at most one player or permanent target are
 * announced here. Other modal shapes keep choosing their modes on resolution.</p>
 *
 * <p>A spell whose "choose one" an opponent chooses ({@link ChooseOneEffect.ModeChooser#AN_OPPONENT}) goes on the stack
 * with its modal unresolved and is announced here too, before anyone gets priority (CR 700.2e): its controller picks
 * which opponent chooses when there is more than one, that opponent chooses among the modes that can be chosen
 * (CR 700.2a), and the controller then chooses the chosen mode's targets among the permanents that player controls
 * (CR 601.2c; Fatal Lore's ruling). The opponent is recorded on the entry as the mode's "that player".</p>
 */
@Slf4j
@Service
public class StackedModalTriggerService {

    private final TriggerTargetCollector triggerTargetCollector;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final InputCompletionService inputCompletionService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final ValidTargetService validTargetService;
    private final PredicateEvaluationService predicateEvaluationService;

    public StackedModalTriggerService(TriggerTargetCollector triggerTargetCollector,
                                      GameQueryService gameQueryService,
                                      GameLogService gameLogService,
                                      @Lazy PlayerInputService playerInputService,
                                      @Lazy InteractionHandlerRegistry interactionHandlerRegistry,
                                      @Lazy InputCompletionService inputCompletionService,
                                      @Lazy ConditionEvaluationService conditionEvaluationService,
                                      @Lazy ValidTargetService validTargetService,
                                      @Lazy PredicateEvaluationService predicateEvaluationService) {
        this.triggerTargetCollector = triggerTargetCollector;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.inputCompletionService = inputCompletionService;
        this.conditionEvaluationService = conditionEvaluationService;
        this.validTargetService = validTargetService;
        this.predicateEvaluationService = predicateEvaluationService;
    }

    /**
     * Begins the mode choice for the lowest stack entry whose mode is still unannounced, removing
     * any such entry that has no legal mode on the way.
     *
     * @return {@code true} if a mode choice is now awaiting input
     */
    public boolean announceNextMode(GameData gameData) {
        for (StackEntry entry : List.copyOf(gameData.stack)) {
            ChooseOneEffect opponentsModal = opponentChosenModal(entry);
            if (opponentsModal != null) {
                if (beginOpponentModeChoice(gameData, entry, opponentsModal)) {
                    return true;
                }
                continue;
            }
            if (!awaitingModeAnnouncement(gameData, entry)) {
                continue;
            }
            if (!interveningConditionHolds(gameData, entry)) {
                gameData.stack.remove(entry);
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s triggered ability's condition is no longer met and it is removed from the stack."));
                log.info("Game {} - {} modal trigger removed (intervening if no longer met)",
                        gameData.id, entry.getCard().getName());
                continue;
            }
            ChooseOneEffect modal = announcedModal(entry);
            Set<String> alreadyChosen = alreadyChosenLabels(gameData, entry, modal);
            if (!alreadyChosen.isEmpty() && modal.options().stream()
                    .allMatch(mode -> alreadyChosen.contains(mode.label()))) {
                gameData.stack.remove(entry);
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s triggered ability has no modes left to choose and is removed from the stack."));
                log.info("Game {} - {} modal trigger removed (every mode already chosen)",
                        gameData.id, entry.getCard().getName());
                continue;
            }
            List<ChooseOneEffect.ChooseOneOption> legalModes = modal.options().stream()
                    .filter(mode -> !alreadyChosen.contains(mode.label()))
                    .filter(mode -> modeTargets(gameData, entry, mode) != null)
                    .toList();
            if (legalModes.isEmpty()) {
                gameData.stack.remove(entry);
                gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                        "'s triggered ability has no legal mode and is removed from the stack."));
                log.info("Game {} - {} modal trigger removed (no legal mode)",
                        gameData.id, entry.getCard().getName());
                continue;
            }
            UUID chooserId = chooserOf(entry);
            ChooseOneEffect legalModal = new ChooseOneEffect(legalModes);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    chooserId, null, null,
                    new ChoiceContext.StackedModalTriggerModeChoice(
                            entry.getTargetableId(), chooserId, entry.getCard(), legalModal),
                    legalModes.stream().map(ChooseOneEffect.ChooseOneOption::label).toList(),
                    entry.getCard().getName() + " - Choose one."));
            log.info("Game {} - {} modal trigger awaiting mode announcement",
                    gameData.id, entry.getCard().getName());
            return true;
        }
        return false;
    }

    /** Applies the announced mode and, if it targets, begins that mode's target choice. */
    public void handleModeChosen(GameData gameData, Player player, String label,
                                 ChoiceContext.StackedModalTriggerModeChoice ctx) {
        ChooseOneEffect.ChooseOneOption mode = ctx.effect().options().stream()
                .filter(option -> option.label().equals(label))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid mode: " + label));
        gameData.interaction.clearAwaitingInput();

        StackEntry entry = findEntry(gameData, ctx.stackEntryId());
        if (entry == null) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (opponentChosenModal(entry) != null) {
            handleOpponentModeChosen(gameData, player, entry, mode, ctx);
            return;
        }
        recordChosenMode(gameData, entry, announcedModal(entry), mode);
        List<UUID> validTargets = modeTargets(gameData, entry, mode);
        List<CardEffect> modeEffects = mode.effects();
        if (entry.getEffectsToResolve().getFirst() instanceof ConditionalEffect conditional) {
            // The intervening "if" is checked again as the chosen mode resolves (CR 603.4).
            modeEffects = modeEffects.stream()
                    .<CardEffect>map(effect -> new ConditionalEffect(conditional.condition(), effect))
                    .toList();
        }
        entry.replaceEffectsToResolve(modeEffects);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + mode.label() + "\" for ", ctx.sourceCard(), "."));
        log.info("Game {} - {} chooses mode \"{}\" for {} as it goes on the stack",
                gameData.id, player.getUsername(), mode.label(), ctx.sourceCard().getName());

        if (validTargets == null || validTargets.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        List<UUID> validPlayers = validTargets.stream().filter(gameData.playerIds::contains).toList();
        List<UUID> validPermanents = validTargets.stream()
                .filter(id -> !gameData.playerIds.contains(id))
                .toList();
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.StackedModalTriggerTarget(
                ctx.stackEntryId(), ctx.sourceCard(), mode.targetFilter()));
        String prompt = ctx.sourceCard().getName() + "'s ability - Choose a target.";
        if (validPlayers.isEmpty()) {
            playerInputService.beginPermanentChoice(gameData, ctx.chooserId(), validPermanents, prompt);
        } else {
            playerInputService.beginAnyTargetChoice(gameData, ctx.chooserId(), validPermanents, validPlayers,
                    prompt);
        }
    }

    /** Records the target chosen for the announced mode on its stack entry. */
    public void handleTargetChosen(GameData gameData, UUID targetId,
                                   PermanentChoiceContext.StackedModalTriggerTarget ctx) {
        StackEntry entry = findEntry(gameData, ctx.stackEntryId());
        if (entry != null) {
            // Keep the defending player the target was chosen against for the resolution legality check.
            UUID defendingPlayerId = defendingPlayerId(gameData, entry);
            if (entry.getAttackedTargetId() == null && defendingPlayerId != null) {
                entry.setAttackedTargetId(defendingPlayerId);
            }
            entry.setTargetId(targetId);
            entry.setTargetFilter(ctx.targetFilter());
            entry.setNonTargeting(false);
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            StackEntry targetSpell = target == null && !gameData.playerIds.contains(targetId)
                    ? findEntry(gameData, targetId) : null;
            if (targetSpell != null) {
                entry.setTargetZone(Zone.STACK);
            }
            String targetName = target != null ? FaceDownVisibility.publicName(gameData, target)
                    : targetSpell != null ? FaceDownVisibility.nameShownTo(gameData, targetSpell, null)
                    : gameData.playerIdToName.get(targetId);
            gameLogService.append(gameData, GameLog.cardThen(ctx.sourceCard(),
                    "'s ability targets " + targetName + "."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Records {@code opponentId} as the opponent who chooses the mode of the spell {@code ctx} names (its controller's
     * pick among several, CR 700.2e); the mode choice follows before anyone gets priority.
     */
    public void handleModeChooserChosen(GameData gameData, UUID opponentId,
                                        PermanentChoiceContext.ModeChoosingOpponent ctx) {
        if (!ctx.opponentIds().contains(opponentId)) {
            throw new IllegalStateException("Invalid player choice");
        }
        gameData.interaction.clearAwaitingInput();
        StackEntry entry = findEntry(gameData, ctx.stackEntryId());
        if (entry != null) {
            entry.setModeChooserId(opponentId);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), ": "
                    + gameData.playerIdToName.get(opponentId) + " chooses its mode."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * The spell's "an opponent chooses one" modal while its mode is unannounced: the entry's only effect, a modal whose
     * {@link ChooseOneEffect#chosenByCaster()} is false.
     */
    private static ChooseOneEffect opponentChosenModal(StackEntry entry) {
        if (entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || entry.getEffectsToResolve().size() != 1) {
            return null;
        }
        return entry.getEffectsToResolve().getFirst() instanceof ChooseOneEffect modal && !modal.chosenByCaster()
                ? modal : null;
    }

    /**
     * Begins the next choice of an opponent-chosen modal: the controller's pick of the opponent who chooses when
     * there is more than one (the only one chooses unasked, CR 608.2d), then that opponent's mode choice among the
     * modes that can be chosen. A spell with no opponent to choose, or no mode that can be chosen, keeps no mode
     * and does nothing as it resolves.
     *
     * @return whether a choice is now awaiting input
     */
    private boolean beginOpponentModeChoice(GameData gameData, StackEntry entry, ChooseOneEffect modal) {
        UUID controllerId = entry.getControllerId();
        if (entry.getModeChooserId() == null || !gameData.playerIds.contains(entry.getModeChooserId())) {
            List<UUID> opponents = SubjectResolutionService
                    .apnapOpponents(gameData, controllerId).stream()
                    .filter(gameData.playerIds::contains)
                    .toList();
            if (opponents.isEmpty()) {
                entry.replaceEffectsToResolve(List.of());
                return false;
            }
            if (opponents.size() > 1) {
                gameData.interaction.setPermanentChoiceContext(
                        new PermanentChoiceContext.ModeChoosingOpponent(entry.getTargetableId(), opponents));
                playerInputService.beginPlayerChoice(gameData, controllerId, opponents,
                        entry.getCard().getName() + " - Choose the opponent who chooses its mode.");
                return true;
            }
            entry.setModeChooserId(opponents.getFirst());
        }
        UUID chooserId = entry.getModeChooserId();
        List<ChooseOneEffect.ChooseOneOption> legalModes = modal.options().stream()
                .filter(mode -> opponentModeTargets(gameData, entry, mode) != null)
                .toList();
        if (legalModes.isEmpty()) {
            entry.replaceEffectsToResolve(List.of());
            return false;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                chooserId, null, null,
                new ChoiceContext.StackedModalTriggerModeChoice(
                        entry.getTargetableId(), chooserId, entry.getCard(), new ChooseOneEffect(legalModes)),
                legalModes.stream().map(ChooseOneEffect.ChooseOneOption::label).toList(),
                entry.getCard().getName() + " - Choose one for " + gameData.playerIdToName.get(controllerId) + "."));
        log.info("Game {} - {} awaiting {}'s mode choice", gameData.id, entry.getCard().getName(),
                gameData.playerIdToName.get(chooserId));
        return true;
    }

    /**
     * Applies the mode the opponent chose: the spell now resolves that mode's effects, and when the mode targets, the
     * spell's controller chooses its targets now, as the casting completes (CR 601.2c).
     */
    private void handleOpponentModeChosen(GameData gameData, Player player, StackEntry entry,
                                          ChooseOneEffect.ChooseOneOption mode,
                                          ChoiceContext.StackedModalTriggerModeChoice ctx) {
        List<UUID> candidates = opponentModeTargets(gameData, entry, mode);
        entry.replaceEffectsToResolve(mode.effects());
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + mode.label() + "\" for ", ctx.sourceCard(), "."));
        log.info("Game {} - {} chooses mode \"{}\" for {}", gameData.id, player.getUsername(), mode.label(),
                ctx.sourceCard().getName());
        if (candidates == null || candidates.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        int maxCount = Math.min(mode.maxTargets(), candidates.size());
        playerInputService.beginMultiPermanentChoice(gameData, entry.getControllerId(), candidates, maxCount,
                new MultiPermanentChoiceContext.TargetsAfterOpponentModeChoice(
                        entry.getTargetableId(), entry.getModeChooserId(), mode.targetFilter()),
                "Choose " + (mode.minTargets() == 0 ? "up to " : "") + maxCount + " target"
                        + (maxCount == 1 ? "" : "s") + " for " + ctx.sourceCard().getName() + ".");
    }

    /**
     * The permanents an opponent-chosen {@code mode} may target: those the mode chooser controls that match the mode's
     * target filter and that the spell can target (shroud, hexproof, protection); an empty list for a mode that targets
     * nothing, or {@code null} when the mode can't be chosen because it has fewer legal targets than it needs
     * (CR 700.2a).
     */
    private List<UUID> opponentModeTargets(GameData gameData, StackEntry entry, ChooseOneEffect.ChooseOneOption mode) {
        if (mode.targetFilter() == null) {
            return List.of();
        }
        List<UUID> candidates = gameData.playerBattlefields
                .getOrDefault(entry.getModeChooserId(), List.of()).stream()
                .filter(Objects::nonNull)
                .filter(permanent -> predicateEvaluationService.matchesFilters(gameData, permanent,
                        Set.of(mode.targetFilter())))
                .filter(permanent -> validTargetService.canPermanentBeTargetedBySpell(
                        gameData, permanent, entry.getCard(), entry.getControllerId()))
                .map(Permanent::getId)
                .toList();
        return candidates.size() < mode.minTargets() ? null : candidates;
    }

    private static boolean awaitingModeAnnouncement(GameData gameData, StackEntry entry) {
        ChooseOneEffect modal = announcedModal(entry);
        if (entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || modal == null
                || !entry.getTargetIds().isEmpty()
                || (entry.getTargetId() != null && !boundPlayerTarget(gameData, entry))) {
            return false;
        }
        return announcesMode(modal);
    }

    /**
     * Whether the mode of a triggered ability that is {@code modal} alone is announced here once the ability is on the
     * stack: one mode of modes that take at most one player or permanent target. The stack door chooses any other
     * modal's mode as the ability goes on the stack (CR 603.3c).
     */
    public static boolean announcesMode(ChooseOneEffect modal) {
        return !modal.optional() && modal.choicesRequired() == 1 && modal.choicesMax() == 1
                && modal.choicesMaxCondition() == null && !modal.isBudgeted()
                && modal.options().stream().allMatch(StackedModalTriggerService::announceableMode);
    }

    /**
     * The "choose one" a trigger announces its mode for: the bare modal, or the modal under an
     * intervening "if" (CR 603.4), whose condition is checked when the mode is announced.
     *
     * @return the modal effect, or {@code null} if the entry is not a single modal (possibly conditional)
     */
    private static ChooseOneEffect announcedModal(StackEntry entry) {
        if (entry.getEffectsToResolve().size() != 1) {
            return null;
        }
        CardEffect effect = entry.getEffectsToResolve().getFirst();
        if (effect instanceof ConditionalEffect conditional) {
            effect = conditional.wrapped();
        }
        return effect instanceof ChooseOneEffect modal ? modal : null;
    }

    /**
     * The labels {@code modal}'s {@link ChooseOneEffect#modeRestriction()} excludes, from the record of the entry's
     * source permanent; none once the source has left the battlefield (a new object starts over).
     */
    private Set<String> alreadyChosenLabels(GameData gameData, StackEntry entry, ChooseOneEffect modal) {
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return Set.of();
        }
        return switch (modal.modeRestriction()) {
            case NONE -> Set.of();
            case NOT_YET_CHOSEN -> Set.copyOf(source.getChosenModeLabels());
            case NOT_YET_CHOSEN_THIS_TURN -> Set.copyOf(source.getChosenModeLabelsThisTurn());
            case NOT_CHOSEN_DURING_LAST_COMBAT -> source.getChosenMode() == null ? Set.of()
                    : Set.of(source.getChosenMode());
        };
    }

    /**
     * Records {@code mode} on the entry's source permanent as {@code modal}'s restriction reads it: it counts as chosen
     * as the ability goes on the stack, even if the ability is countered later.
     */
    private void recordChosenMode(GameData gameData, StackEntry entry, ChooseOneEffect modal,
                                  ChooseOneEffect.ChooseOneOption mode) {
        Permanent source = modal == null || entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }
        switch (modal.modeRestriction()) {
            case NONE -> { }
            case NOT_YET_CHOSEN -> source.getChosenModeLabels().add(mode.label());
            case NOT_YET_CHOSEN_THIS_TURN -> source.getChosenModeLabelsThisTurn().add(mode.label());
            case NOT_CHOSEN_DURING_LAST_COMBAT -> source.setChosenMode(mode.label());
        }
    }

    private boolean interveningConditionHolds(GameData gameData, StackEntry entry) {
        if (!(entry.getEffectsToResolve().getFirst() instanceof ConditionalEffect conditional)) {
            return true;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        ConditionContext context = source == null
                ? ConditionContext.forCard(entry.getCard(), entry.getControllerId())
                : ConditionContext.forPermanent(source, entry.getControllerId());
        return conditionEvaluationService.isMet(gameData, conditional.condition(), context);
    }

    private static boolean announceableMode(ChooseOneEffect.ChooseOneOption mode) {
        if (mode.targetFilters() != null || mode.effectFactory() != null) {
            return false;
        }
        for (CardEffect effect : mode.effects()) {
            if (effect.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD)
                    || effect.targetSpec().admits(TargetPredicate.Kind.EXILED_CARD)
                    || effect.targetSpec().admits(TargetPredicate.Kind.SPELL)
                    || EffectResolution.targetsSpellOnStack(effect)) {
                return false;
            }
        }
        return !choosesTarget(mode) || (mode.minTargets() == 1 && mode.maxTargets() == 1);
    }

    private static boolean choosesTarget(ChooseOneEffect.ChooseOneOption mode) {
        return mode.effects().stream().anyMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PLAYER)
                        || effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
    }

    /**
     * The legal targets of {@code mode} for this entry: an empty list when the mode chooses no
     * target, or {@code null} when the mode can't be chosen because it has no legal target.
     */
    private List<UUID> modeTargets(GameData gameData, StackEntry entry, ChooseOneEffect.ChooseOneOption mode) {
        if (!choosesTarget(mode) || usesBoundDamagedPlayer(gameData, entry, mode)) {
            return List.of();
        }
        Permanent source = entry.getSourcePermanentId() == null
                ? null : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        TriggerTargetCollector.Result result = triggerTargetCollector.collect(
                gameData, mode.effects(), mode.targetFilter(), entry.getControllerId(), entry.getCard(),
                TriggerTargetCollector.Options.ATTACK, source, defendingPlayerId(gameData, entry));
        return result.validTargets().isEmpty() ? null : List.copyOf(result.validTargets());
    }

    /**
     * A modal's "that player" modes act on the player the trigger event was about, which the entry already carries
     * as its non-targeting target (a damage-to-a-player trigger's damaged player), so choosing such a mode chooses no
     * target: a mode with no permanent target and no player target the card declares. A mode that declares a player
     * target filter ("target player gains 1 life", Relic Bind) chooses that player, never "that player".
     */
    private static boolean usesBoundDamagedPlayer(GameData gameData, StackEntry entry,
                                                  ChooseOneEffect.ChooseOneOption mode) {
        return boundPlayerTarget(gameData, entry)
                && !(mode.targetFilter() instanceof PlayerPredicateTargetFilter)
                && mode.effects().stream().noneMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT)
                        || entry.getCard().hasEffectTargetIndex(effect));
    }

    private static boolean boundPlayerTarget(GameData gameData, StackEntry entry) {
        return entry.isNonTargeting() && entry.getTargetId() != null
                && gameData.playerIds.contains(entry.getTargetId());
    }

    private static UUID defendingPlayerId(GameData gameData, StackEntry entry) {
        if (entry.getDefendingPlayerId() != null) {
            return entry.getDefendingPlayerId();
        }
        if (entry.getAttackedTargetId() != null && gameData.playerIds.contains(entry.getAttackedTargetId())) {
            return entry.getAttackedTargetId();
        }
        return boundPlayerTarget(gameData, entry) ? entry.getTargetId() : null;
    }

    private static UUID chooserOf(StackEntry entry) {
        return entry.getActivePlayerId() != null ? entry.getActivePlayerId() : entry.getControllerId();
    }

    private static StackEntry findEntry(GameData gameData, UUID stackEntryId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getTargetableId().equals(stackEntryId))
                .findFirst()
                .orElse(null);
    }
}
