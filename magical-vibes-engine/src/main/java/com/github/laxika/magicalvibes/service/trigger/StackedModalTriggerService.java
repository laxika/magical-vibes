package com.github.laxika.magicalvibes.service.trigger;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CombatDamageTriggerContextEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.HullbreakerHorrorTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellToHandEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
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

    public StackedModalTriggerService(TriggerTargetCollector triggerTargetCollector,
                                      GameQueryService gameQueryService,
                                      GameLogService gameLogService,
                                      @Lazy PlayerInputService playerInputService,
                                      @Lazy InteractionHandlerRegistry interactionHandlerRegistry,
                                      @Lazy InputCompletionService inputCompletionService,
                                      @Lazy ConditionEvaluationService conditionEvaluationService) {
        this.triggerTargetCollector = triggerTargetCollector;
        this.gameQueryService = gameQueryService;
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.inputCompletionService = inputCompletionService;
        this.conditionEvaluationService = conditionEvaluationService;
    }

    /**
     * Begins the mode choice for the lowest stack entry whose mode is still unannounced, removing
     * any such entry that has no legal mode on the way.
     *
     * @return {@code true} if a mode choice is now awaiting input
     */
    public boolean announceNextMode(GameData gameData) {
        for (StackEntry entry : List.copyOf(gameData.stack)) {
            if (awaitingHullbreakerHorrorAnnouncement(entry)) {
                announceHullbreakerHorrorMode(gameData, entry);
                return true;
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
            List<ChooseOneEffect.ChooseOneOption> legalModes = modal.options().stream()
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
        if (awaitingHullbreakerHorrorAnnouncement(entry)) {
            handleHullbreakerHorrorModeChosen(gameData, player, mode, ctx, entry);
            return;
        }
        List<UUID> validTargets = modeTargets(gameData, entry, mode);
        List<CardEffect> modeEffects = mode.effects();
        if (entry.getEffectsToResolve().getFirst() instanceof ConditionalEffect conditional
                && conditional.interveningIf()) {
            // The intervening "if" is checked again as the chosen mode resolves (CR 603.4).
            modeEffects = modeEffects.stream()
                    .<CardEffect>map(effect -> new ConditionalEffect(conditional.condition(), effect, true))
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
            String targetName = target != null ? target.getCard().getName()
                    : targetSpell != null ? targetSpell.getCard().getName()
                    : gameData.playerIdToName.get(targetId);
            gameLogService.append(gameData, GameLog.cardThen(ctx.sourceCard(),
                    "'s ability targets " + targetName + "."));
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    private static boolean awaitingHullbreakerHorrorAnnouncement(StackEntry entry) {
        return entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getEffectsToResolve().size() == 1
                && entry.getEffectsToResolve().getFirst() instanceof HullbreakerHorrorTriggerEffect;
    }

    /**
     * Hullbreaker Horror's "choose up to one" trigger announces its mode as it goes on the stack
     * (CR 603.3c); a mode with no legal target can't be chosen, and choosing none is always possible.
     */
    private void announceHullbreakerHorrorMode(GameData gameData, StackEntry entry) {
        UUID controllerId = entry.getControllerId();
        List<ChooseOneEffect.ChooseOneOption> modes = new ArrayList<>();
        if (!opponentSpellIds(gameData, controllerId).isEmpty()) {
            modes.add(new ChooseOneEffect.ChooseOneOption(
                    ChoiceContext.HullbreakerHorrorModeChoice.SPELL, new ReturnTargetSpellToHandEffect()));
        }
        if (!nonlandPermanentIds(gameData).isEmpty()) {
            modes.add(new ChooseOneEffect.ChooseOneOption(
                    ChoiceContext.HullbreakerHorrorModeChoice.PERMANENT, ReturnToHandEffect.target()));
        }
        modes.add(new ChooseOneEffect.ChooseOneOption(ChoiceContext.HullbreakerHorrorModeChoice.NONE,
                List.of(), null, null, 0, 0, false, null, null));
        ChooseOneEffect modal = new ChooseOneEffect(modes);
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                controllerId, null, null,
                new ChoiceContext.StackedModalTriggerModeChoice(
                        entry.getTargetableId(), controllerId, entry.getCard(), modal),
                modes.stream().map(ChooseOneEffect.ChooseOneOption::label).toList(),
                entry.getCard().getName() + " - Choose up to one."));
        log.info("Game {} - {} modal trigger awaiting mode announcement",
                gameData.id, entry.getCard().getName());
    }

    private void handleHullbreakerHorrorModeChosen(GameData gameData, Player player,
                                                   ChooseOneEffect.ChooseOneOption mode,
                                                   ChoiceContext.StackedModalTriggerModeChoice ctx,
                                                   StackEntry entry) {
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + mode.label() + "\" for ", ctx.sourceCard(), "."));
        if (mode.effects().isEmpty()) {
            gameData.stack.remove(entry);
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s triggered ability has no mode chosen and is removed from the stack."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        boolean spellMode = mode.effects().getFirst() instanceof ReturnTargetSpellToHandEffect;
        List<UUID> validTargets = spellMode
                ? opponentSpellIds(gameData, ctx.chooserId()) : nonlandPermanentIds(gameData);
        entry.replaceEffectsToResolve(mode.effects());
        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.StackedModalTriggerTarget(
                ctx.stackEntryId(), ctx.sourceCard(), null));
        if (spellMode) {
            playerInputService.beginAnyTargetChoice(gameData, ctx.chooserId(), validTargets, List.of(),
                    ctx.sourceCard().getName() + " - Choose target spell you don't control.");
        } else {
            playerInputService.beginPermanentChoice(gameData, ctx.chooserId(), validTargets,
                    ctx.sourceCard().getName() + " - Choose target nonland permanent.");
        }
    }

    private static List<UUID> opponentSpellIds(GameData gameData, UUID controllerId) {
        return gameData.stack.stream()
                .filter(se -> se.getEntryType() != StackEntryType.ACTIVATED_ABILITY
                        && se.getEntryType() != StackEntryType.TRIGGERED_ABILITY)
                .filter(se -> !controllerId.equals(se.getControllerId()))
                .map(StackEntry::getTargetableId)
                .toList();
    }

    private static List<UUID> nonlandPermanentIds(GameData gameData) {
        List<UUID> ids = new ArrayList<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                if (!permanent.getCard().hasType(CardType.LAND)) {
                    ids.add(permanent.getId());
                }
            }
        }
        return ids;
    }

    private static boolean awaitingModeAnnouncement(GameData gameData, StackEntry entry) {
        ChooseOneEffect modal = announcedModal(entry);
        if (entry.getEntryType() != StackEntryType.TRIGGERED_ABILITY
                || modal == null
                || modal.optional() || modal.choicesRequired() != 1 || modal.choicesMax() != 1
                || modal.choicesMaxCondition() != null || modal.isBudgeted()
                || !entry.getTargetIds().isEmpty()
                || (entry.getTargetId() != null && !boundPlayerTarget(gameData, entry))) {
            return false;
        }
        return modal.options().stream().allMatch(StackedModalTriggerService::announceableMode);
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
        if (effect instanceof ConditionalEffect conditional && conditional.interveningIf()) {
            effect = conditional.wrapped();
        }
        return effect instanceof ChooseOneEffect modal ? modal : null;
    }

    private boolean interveningConditionHolds(GameData gameData, StackEntry entry) {
        if (!(entry.getEffectsToResolve().getFirst() instanceof ConditionalEffect conditional)
                || !conditional.interveningIf()) {
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
     * A combat-damage modal's "that player" modes act on the damaged player the trigger already
     * carries, so choosing such a mode chooses no target.
     */
    private static boolean usesBoundDamagedPlayer(GameData gameData, StackEntry entry,
                                                  ChooseOneEffect.ChooseOneOption mode) {
        ChooseOneEffect modal = announcedModal(entry);
        return boundPlayerTarget(gameData, entry)
                && modal != null
                && modal.combatDamageTriggerContext() == CombatDamageTriggerContextEffect.TriggerContext.DAMAGED_PLAYER
                && mode.effects().stream().noneMatch(effect ->
                effect.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
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
