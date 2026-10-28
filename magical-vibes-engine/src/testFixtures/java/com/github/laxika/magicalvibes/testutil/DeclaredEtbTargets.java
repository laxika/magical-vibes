package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.MultiTargetConstraint;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.service.battlefield.ETBTokenTargetService;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Lets a test name a permanent spell's ETB targets in the cast call, as a shorthand for answering the
 * ETB trigger's target prompt.
 *
 * <p>A permanent spell's ETB abilities choose their targets as they are put on the stack
 * (CR 603.3d), so the engine never takes them while the spell is cast. The harness casts the spell
 * without them, checks them against the card's ETB target declaration right away (so an illegal
 * choice still fails at the cast call), and answers each of the card's ETB target prompts with them
 * once those prompts open. A prompt that none of the declared targets fits is left to the test, so a
 * card with several ETB abilities can declare targets for one of them and answer the others itself.</p>
 */
final class DeclaredEtbTargets {

    /**
     * The targets to hand to the engine's cast call, plus the ETB targets the harness answers later
     * ({@code null} when the cast keeps its targets).
     */
    record CastTargets(UUID targetId, List<UUID> targetIds, List<UUID> etbTargets) {}

    private static final int ANSWER_LIMIT = 1000;

    private final GameData gameData;
    private final GameService gameService;
    private final TargetLegalityService targetLegalityService;
    private final ETBTokenTargetService etbTokenTargetService;
    private final List<Player> players;
    private final List<Declaration> declarations = new ArrayList<>();

    DeclaredEtbTargets(GameData gameData, GameService gameService, TargetLegalityService targetLegalityService,
                       ETBTokenTargetService etbTokenTargetService, List<Player> players) {
        this.gameData = gameData;
        this.gameService = gameService;
        this.targetLegalityService = targetLegalityService;
        this.etbTokenTargetService = etbTokenTargetService;
        this.players = players;
    }

    /**
     * Splits a cast helper's targets: a permanent spell whose only targets belong to its ETB
     * abilities is cast with none, and the targets are validated now for its ETB prompts. Any other
     * spell keeps its targets. {@code targetIds} is {@code null} when the cast helper takes no target
     * list; an explicit empty list declares that the ETB abilities choose no targets.
     * {@code permanentFace} is false for helpers that may instead cast an instant or sorcery face of
     * the card (an Omen or Adventure), whose targets are left alone.
     */
    CastTargets prepare(Player player, Card card, boolean permanentFace, UUID targetId, List<UUID> targetIds,
                        int xValue, boolean kicked) {
        List<UUID> listed = targetIds == null ? List.of() : targetIds;
        if (card != null && targetId == null && listed.size() > 1 && auraWithEtbTargetGroup(card)) {
            // An Aura's enchant target is its spell's own target; the rest belong to its ETB ability.
            List<UUID> etbTargets = List.copyOf(listed.subList(1, listed.size()));
            targetLegalityService.validateSpellTargetGroup(gameData, card, 1, etbTargets, player.getId(),
                    xValue, kicked);
            return new CastTargets(listed.getFirst(), List.of(), etbTargets);
        }
        if (card == null || targetId == null && targetIds == null
                || !EffectResolution.targetsOnlyWithEtbAbilities(card)
                || !permanentFace && hasSpellBackFace(card)) {
            return new CastTargets(targetId, listed, null);
        }
        List<UUID> etbTargets = new ArrayList<>();
        if (targetId != null) {
            etbTargets.add(targetId);
        }
        etbTargets.addAll(listed);
        if (etbTargets.isEmpty()) {
            if (card.getMaxTargets() > 0
                    && (card.getMultiTargetConstraint() == MultiTargetConstraint.ONE_PER_OPPONENT_IF_ABLE
                    || card.getMultiTargetConstraint() == MultiTargetConstraint.ONE_PER_CONTROLLER_IF_ABLE)) {
                // Declaring no targets is only legal when each player's constraint has nothing to target.
                targetLegalityService.validateMultiSpellTargets(gameData, card, List.of(), player.getId(), xValue,
                        kicked);
            }
            return new CastTargets(null, List.of(), List.of());
        }
        List<SpellTarget> etbGroups = etbTargetGroups(card);
        if (!listed.isEmpty() && etbGroups.size() < card.getSpellTargets().size()
                && etbGroups.stream().allMatch(group -> group.getMinTargets() == group.getMaxTargets())
                && etbGroups.stream().mapToInt(SpellTarget::getMaxTargets).sum() == etbTargets.size()) {
            // Other groups belong to the card's other triggered abilities (Hunter's Talent's attack
            // trigger), so only the ETB ability's groups are checked against the declared targets.
            int offset = 0;
            for (SpellTarget group : etbGroups) {
                targetLegalityService.validateSpellTargetGroup(gameData, card, group.getIndex(),
                        etbTargets.subList(offset, offset + group.getMaxTargets()), player.getId(), xValue, kicked);
                offset += group.getMaxTargets();
            }
        } else if (!listed.isEmpty() && card.getMaxTargets() > 0) {
            targetLegalityService.validateMultiSpellTargets(gameData, card, etbTargets, player.getId(), xValue, kicked);
            validateEtbPermanentsTargetable(card, etbTargets, player.getId());
        } else if (modalEtbOption(card, xValue) != null) {
            // The mode is chosen as the spell is cast (xValue), so the target is checked against it.
            ChooseOneEffect.ChooseOneOption option = modalEtbOption(card, xValue);
            for (UUID etbTarget : etbTargets) {
                targetLegalityService.validateEffectTargetInZone(gameData, card, option.effects(),
                        etbTarget, Zone.BATTLEFIELD, xValue, player.getId());
                targetLegalityService.validatePermanentTargetFilter(gameData, option.targetFilter(), card,
                        etbTarget, player.getId());
            }
        } else {
            for (UUID etbTarget : etbTargets) {
                targetLegalityService.validateSpellTargeting(gameData, card, etbTarget, null, player.getId(),
                        true, xValue, kicked);
                validateAgainstEtbEffects(card, etbTarget, player.getId(), xValue);
            }
        }
        return new CastTargets(null, List.of(), List.copyOf(etbTargets));
    }

    /**
     * Checks a battlefield or player target against the card's ETB effects, since the spell-slot
     * validation sees none of them: it must be legal for at least one ETB effect that targets.
     */
    /**
     * Rejects a declared permanent target that the card's ETB ability cannot target, such as a creature
     * with protection from the ability's source. The source is the card itself, so its subtypes and
     * colors decide the protection that applies.
     */
    private void validateEtbPermanentsTargetable(Card card, List<UUID> targets, UUID controllerId) {
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (!targets.contains(permanent.getId())) {
                    continue;
                }
                targetLegalityService.checkSpellPermanentTargetableReason(gameData, permanent, card, controllerId)
                        .ifPresent(reason -> {
                            throw new IllegalStateException(reason);
                        });
            }
        }
    }

    private void validateAgainstEtbEffects(Card card, UUID target, UUID controllerId, int xValue) {
        boolean onBattlefield = gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .anyMatch(permanent -> permanent.getId().equals(target));
        if (!onBattlefield && !gameData.playerIds.contains(target)) {
            return;
        }
        IllegalStateException firstRejection = null;
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect.targetSpec().targetPredicate() == null) {
                continue;
            }
            try {
                targetLegalityService.validateEffectTargetInZone(gameData, card, List.of(effect), target,
                        Zone.BATTLEFIELD, xValue, controllerId);
                return;
            } catch (IllegalStateException rejection) {
                if (firstRejection == null) {
                    firstRejection = rejection;
                }
            }
        }
        if (firstRejection != null) {
            throw firstRejection;
        }
    }

    /** The ETB mode a modal enters-the-battlefield ability picks at cast time via {@code mode}, or null. */
    private static ChooseOneEffect.ChooseOneOption modalEtbOption(Card card, int mode) {
        for (CardEffect effect : card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)) {
            if (effect instanceof ChooseOneEffect choice && mode >= 0 && mode < choice.options().size()) {
                return choice.options().get(mode);
            }
        }
        return null;
    }

    /** The card's target groups that belong to its enter-the-battlefield ability (or to no ability). */
    private static List<SpellTarget> etbTargetGroups(Card card) {
        List<CardEffect> etbEffects = card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD);
        return card.getSpellTargets().stream()
                .filter(group -> card.getEffectsBoundToTargetGroup(group.getIndex()).stream()
                        .allMatch(effect -> etbEffects.stream().anyMatch(etb -> etb == effect)))
                .toList();
    }

    /** Remembers the ETB targets {@link #prepare} took off a cast the engine then accepted. */
    void declare(Card card, CastTargets prepared) {
        if (card == null || prepared.etbTargets() == null) {
            return;
        }
        declarations.removeIf(declaration -> declaration.cardId.equals(card.getId()));
        declarations.add(new Declaration(card.getId(), prepared.etbTargets()));
        answerOpenPrompts();
    }

    /** Answers every open ETB target prompt of a card whose targets were declared at cast. */
    void answerOpenPrompts() {
        for (int answered = 0; answered < ANSWER_LIMIT && !declarations.isEmpty(); answered++) {
            declarations.removeIf(declaration -> !declaration.used && !isOnStackOrBattlefield(declaration.cardId));
            if (!gameData.interaction.isAwaitingInput() || !answerActivePrompt()) {
                return;
            }
        }
    }

    private boolean answerActivePrompt() {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active instanceof PendingInteraction.PermanentChoice choice) {
            if (choice.context() instanceof PermanentChoiceContext.ETBTokenTargetTrigger single) {
                return answerSingleTarget(choice, single);
            }
            if (choice.context() instanceof PermanentChoiceContext.ETBTokenMultiTargetTrigger multi) {
                return answerTargetSlot(choice, multi);
            }
            return false;
        }
        if (active instanceof PendingInteraction.MultiPermanentChoice choice
                && etbTargetGroup(choice.context()) != null) {
            return answerTargetGroup(choice, etbTargetGroup(choice.context()));
        }
        if (active instanceof PendingInteraction.MultiPermanentChoice choice
                && choice.context() instanceof MultiPermanentChoiceContext.EtbOptionalTarget optional) {
            return answerOptionalTarget(choice, optional.pending());
        }
        return false;
    }

    /** Answers an optional single-target prompt, choosing no target when none were declared. */
    private boolean answerOptionalTarget(PendingInteraction.MultiPermanentChoice choice,
                                         PermanentChoiceContext.ETBTokenTargetTrigger single) {
        Declaration declaration = find(single.sourceCard(), single.sourcePermanentId());
        if (declaration == null) {
            return false;
        }
        List<UUID> chosen = declaration.targetIds.stream()
                .filter(id -> choice.validIds().contains(id) || choice.validPlayerIds().contains(id))
                .limit(1)
                .toList();
        if (chosen.isEmpty() && !declaration.targetIds.isEmpty()) {
            return false;
        }
        answer(choice.playerId(), new InteractionAnswer.PermanentsChosen(chosen), declaration, single.sourceCard());
        return true;
    }

    private boolean answerSingleTarget(PendingInteraction.PermanentChoice choice,
                                       PermanentChoiceContext.ETBTokenTargetTrigger single) {
        Declaration declaration = find(single.sourceCard(), single.sourcePermanentId());
        if (declaration == null) {
            return false;
        }
        if (declaration.targetIds.isEmpty()) {
            if (!etbTokenTargetService.controllerChoiceDeclines(gameData, single)) {
                throw new IllegalStateException(single.sourceCard().getName()
                        + "'s ETB ability was declared with no targets, but its prompt cannot be declined");
            }
            answer(choice.playerId(), new InteractionAnswer.PermanentChosen(single.controllerId()), declaration,
                    single.sourceCard());
            return true;
        }
        UUID target = declaration.targetIds.stream().filter(choice.validIds()::contains).findFirst().orElse(null);
        if (target == null) {
            return false;
        }
        answer(choice.playerId(), new InteractionAnswer.PermanentChosen(target), declaration, single.sourceCard());
        return true;
    }

    private boolean answerTargetSlot(PendingInteraction.PermanentChoice choice,
                                     PermanentChoiceContext.ETBTokenMultiTargetTrigger multi) {
        Declaration declaration = find(multi.sourceCard(), multi.sourcePermanentId());
        if (declaration == null) {
            return false;
        }
        List<UUID> candidates = declaration.targetIds.stream()
                .filter(id -> !chosenEarlier(multi, id))
                .filter(choice.validIds()::contains)
                .toList();
        // A card whose groups may share targets still gets distinct targets when the declaration
        // lists enough of them; a shared target is only reused when nothing else is left.
        UUID next = candidates.stream()
                .filter(id -> !multi.chosenTargetsSoFar().contains(id))
                .findFirst()
                .orElse(candidates.isEmpty() ? null : candidates.getFirst());
        if (next != null) {
            answer(choice.playerId(), new InteractionAnswer.PermanentChosen(next), declaration, multi.sourceCard());
            return true;
        }
        // Choosing yourself stops adding targets to this group: done once the ability has taken its
        // declared targets, when none were declared, or to reach a later group that the remaining
        // declared targets belong to. A group sized by the game state (X, kicker payments) that took
        // none of the declared targets is left to the test, as such groups always chose their
        // targets as the trigger was put on the stack.
        boolean stopOffered = choice.validPlayerIds().contains(multi.controllerId())
                && etbTokenTargetService.controllerChoiceStopsGroup(gameData, multi);
        boolean groupTookDeclaredTargets = currentGroupTargets(multi).stream()
                .anyMatch(declaration.targetIds::contains);
        boolean abilityTookDeclaredTargets = multi.chosenTargetsSoFar().stream()
                .anyMatch(declaration.targetIds::contains);
        boolean declaredTargetsRemain = declaration.targetIds.stream()
                .anyMatch(id -> !multi.chosenTargetsSoFar().contains(id));
        if (stopOffered && (groupTookDeclaredTargets || declaration.targetIds.isEmpty()
                || abilityTookDeclaredTargets && !hasDynamicSize(multi)
                || hasLaterTargetGroup(multi) && declaredTargetsRemain)) {
            answer(choice.playerId(), new InteractionAnswer.PermanentChosen(multi.controllerId()), declaration,
                    multi.sourceCard());
            return true;
        }
        return false;
    }

    private boolean answerTargetGroup(PendingInteraction.MultiPermanentChoice choice,
                                      PermanentChoiceContext.ETBTokenMultiTargetTrigger multi) {
        Declaration declaration = find(multi.sourceCard(), multi.sourcePermanentId());
        if (declaration == null) {
            return false;
        }
        List<UUID> chosen = declaration.targetIds.stream()
                .filter(id -> !chosenEarlier(multi, id))
                .filter(id -> choice.validIds().contains(id) || choice.validPlayerIds().contains(id)
                        || choice.validCardIds().contains(id))
                .limit(choice.maxCount())
                .toList();
        if (chosen.isEmpty() && !declaration.targetIds.isEmpty()) {
            return false;
        }
        answer(choice.playerId(), new InteractionAnswer.PermanentsChosen(chosen), declaration, multi.sourceCard());
        return true;
    }

    /**
     * True when this ability already chose {@code id}, in the current group or, unless the card lets
     * groups share targets, in an earlier one.
     */
    private static boolean chosenEarlier(PermanentChoiceContext.ETBTokenMultiTargetTrigger multi, UUID id) {
        return multi.sourceCard().isAllowSharedTargets()
                ? currentGroupTargets(multi).contains(id)
                : multi.chosenTargetsSoFar().contains(id);
    }

    private static List<UUID> currentGroupTargets(PermanentChoiceContext.ETBTokenMultiTargetTrigger multi) {
        List<UUID> chosen = multi.chosenTargetsSoFar();
        int currentGroupStart = Math.max(0, chosen.size() - multi.chosenInCurrentGroup());
        return chosen.subList(currentGroupStart, chosen.size());
    }

    private static boolean hasDynamicSize(PermanentChoiceContext.ETBTokenMultiTargetTrigger multi) {
        List<SpellTarget> groups = multi.sourceCard().getSpellTargets();
        if (multi.currentGroupIndex() >= groups.size()) {
            return false;
        }
        SpellTarget group = groups.get(multi.currentGroupIndex());
        return group.getDynamicMaxTargets() != null || group.isXScaled();
    }

    private static boolean hasLaterTargetGroup(PermanentChoiceContext.ETBTokenMultiTargetTrigger multi) {
        Card card = multi.sourceCard();
        return multi.effects().stream()
                .anyMatch(effect -> card.getEffectTargetIndex(effect) > multi.currentGroupIndex());
    }

    private static PermanentChoiceContext.ETBTokenMultiTargetTrigger etbTargetGroup(
            MultiPermanentChoiceContext context) {
        if (context instanceof MultiPermanentChoiceContext.EtbPlayerTargetGroup group) {
            return group.pending();
        }
        if (context instanceof MultiPermanentChoiceContext.EtbGraveyardCardTargetGroup group) {
            return group.pending();
        }
        return null;
    }

    private void answer(UUID playerId, InteractionAnswer answer, Declaration declaration, Card card) {
        declaration.used = true;
        Player player = players.stream().filter(candidate -> candidate.getId().equals(playerId))
                .findFirst().orElseThrow();
        gameService.handleInteractionAnswer(gameData, player, answer);
        if (!hasEtbPromptFor(card)) {
            declarations.remove(declaration);
        }
    }

    private boolean hasEtbPromptFor(Card card) {
        if (gameData.interaction.isAwaitingInput() && promptsFor(gameData.interaction.activeInteraction(), card)) {
            return true;
        }
        return gameData.pendingInteractions.stream().anyMatch(pending -> promptsFor(pending, card));
    }

    private static boolean promptsFor(Object interaction, Card card) {
        Object context = interaction;
        if (interaction instanceof PendingInteraction.PermanentChoice choice) {
            context = choice.context();
        } else if (interaction instanceof PendingInteraction.MultiPermanentChoice choice
                && etbTargetGroup(choice.context()) != null) {
            context = etbTargetGroup(choice.context());
        } else if (interaction instanceof PendingInteraction.MultiPermanentChoice choice
                && choice.context() instanceof MultiPermanentChoiceContext.EtbOptionalTarget optional) {
            context = optional.pending();
        }
        if (context instanceof PermanentChoiceContext.ETBTokenTargetTrigger single) {
            return single.sourceCard().getId().equals(card.getId());
        }
        return context instanceof PermanentChoiceContext.ETBTokenMultiTargetTrigger multi
                && multi.sourceCard().getId().equals(card.getId());
    }

    private Declaration find(Card sourceCard, UUID sourcePermanentId) {
        for (Declaration declaration : declarations) {
            if (declaration.cardId.equals(sourceCard.getId())) {
                return declaration;
            }
            if (sourcePermanentId != null) {
                var permanent = gameData.playerBattlefields.values().stream().flatMap(List::stream)
                        .filter(candidate -> candidate.getId().equals(sourcePermanentId))
                        .findFirst().orElse(null);
                if (permanent != null && permanent.getOriginalCard().getId().equals(declaration.cardId)) {
                    return declaration;
                }
            }
        }
        return null;
    }

    private boolean isOnStackOrBattlefield(UUID cardId) {
        return gameData.stack.stream().anyMatch(entry -> entry.getCard() != null
                && entry.getCard().getId().equals(cardId))
                || gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .anyMatch(permanent -> permanent.getCard().getId().equals(cardId)
                        || permanent.getOriginalCard().getId().equals(cardId));
    }

    private static boolean auraWithEtbTargetGroup(Card card) {
        return card.isAuraThatRequiresAttachment() && card.getSpellTargets().size() == 2
                && card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).stream()
                .anyMatch(effect -> card.getEffectTargetIndex(effect) == 1);
    }

    private static boolean hasSpellBackFace(Card card) {
        Card back = card.getBackFaceCard();
        return back != null && (back.hasType(CardType.INSTANT) || back.hasType(CardType.SORCERY));
    }

    private static final class Declaration {
        private final UUID cardId;
        private final List<UUID> targetIds;
        private boolean used;

        private Declaration(UUID cardId, List<UUID> targetIds) {
            this.cardId = cardId;
            this.targetIds = targetIds;
        }
    }
}
