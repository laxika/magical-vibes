package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.action.ReturnExiledCardToHandAtNextEndStep;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DivisionMode;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

/**
 * Casts a split card from a hand, graveyard, library or exile during resolution or through a "you may
 * cast" permission. The player chooses the half as the spell is cast (CR 709.3), then that half's
 * targets (CR 601.2c), then pays that half's mana cost when the cast isn't free (CR 601.2h), before
 * the spell is put onto the stack. Only the chosen half is evaluated to see if it can be cast
 * (CR 709.3a), so a half is offered only if it fits the cast's mana value limit and has a legal
 * set of targets. Both halves may be cast together only from the caster's own hand, and only by a
 * card with fuse (CR 702.102a).
 */
@Slf4j
@Component
public class SplitCardCastSupport {

    static final String CHOOSE_TARGET = "Choose another target";
    static final String DONE = "Done";

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final DealDividedDamageSupport dealDividedDamageSupport;
    private final InputCompletionService inputCompletionService;
    private final ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    private final GameQueryService gameQueryService;
    private final SpellCastingService spellCastingService;
    private final CopySupport copySupport;
    private final PermanentRemovalService permanentRemovalService;
    private final LifeSupport lifeSupport;
    private final CastingPermissionService castingPermissionService;

    public SplitCardCastSupport(GameLogService gameLogService,
                                PlayerInputService playerInputService,
                                InteractionHandlerRegistry interactionHandlerRegistry,
                                TriggerCollectionService triggerCollectionService,
                                ExileCastTargetSupport exileCastTargetSupport,
                                DealDividedDamageSupport dealDividedDamageSupport,
                                @Lazy InputCompletionService inputCompletionService,
                                @Lazy ExileFreeCastQueueSupport exileFreeCastQueueSupport,
                                @Lazy GameQueryService gameQueryService,
                                @Lazy SpellCastingService spellCastingService,
                                CopySupport copySupport,
                                @Lazy PermanentRemovalService permanentRemovalService,
                                LifeSupport lifeSupport,
                                @Lazy CastingPermissionService castingPermissionService) {
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
        this.triggerCollectionService = triggerCollectionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.dealDividedDamageSupport = dealDividedDamageSupport;
        this.inputCompletionService = inputCompletionService;
        this.exileFreeCastQueueSupport = exileFreeCastQueueSupport;
        this.gameQueryService = gameQueryService;
        this.spellCastingService = spellCastingService;
        this.copySupport = copySupport;
        this.permanentRemovalService = permanentRemovalService;
        this.lifeSupport = lifeSupport;
        this.castingPermissionService = castingPermissionService;
    }

    /**
     * Whether {@code card} is an instant or sorcery split card whose halves are the modes of its
     * spell. Rooms are split cards too, but they are cast as permanent spells with a door to unlock,
     * which this support doesn't cast.
     */
    public static boolean isSplitCard(Card card) {
        return card.getSplitHalves().size() >= 2 && splitModal(card) != null
                && !card.getSubtypes().contains(CardSubtype.ROOM);
    }

    /**
     * Whether {@code modeIndex} of a split card's modal spell casts both halves together, which
     * only a card with fuse cast from its owner's hand may do (CR 702.102a).
     */
    public static boolean isFusedMode(Card card, int modeIndex) {
        return isSplitCard(card) && modeIndex >= card.getSplitHalves().size();
    }

    /**
     * Offers the halves of {@code cast}'s card that can be cast. Returns false, leaving the card where
     * it was taken from, when none can.
     */
    public boolean begin(GameData gameData, SplitCardCast cast) {
        Card card = cast.physicalCard();
        ChooseOneEffect modal = splitModal(card);
        List<Integer> offered = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            if (isCastableMode(gameData, cast, modal, modeIndex)) {
                offered.add(modeIndex);
            }
        }
        if (offered.isEmpty()) {
            returnToSourceZone(gameData, cast);
            return false;
        }

        List<String> labels = offered.stream().map(modal.options()::get)
                .map(ChooseOneEffect.ChooseOneOption::label).toList();
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                cast.controllerId(), null, null,
                new ChoiceContext.SplitFreeCastHalfChoice(cast, modal, offered),
                labels, card.getName() + " — Choose which half to cast."));
        return true;
    }

    /**
     * Casts the exiled split card {@code card} during resolution, paying the chosen half's mana cost
     * unless {@code payManaCost} is false. The card stays in exile until the spell is cast; the
     * exile permissions on it decide whether mana of any type or life equal to the half's mana
     * value pays that cost and where the spell goes once it resolves. Returns false, without
     * casting, when the card can't be cast or no half can; a copy then ceases to exist.
     */
    public boolean beginFromExile(GameData gameData, UUID controllerId, Card card, boolean payManaCost,
                                  boolean copy, boolean putOnLibraryBottomInsteadOfGraveyard, int lifeLossOnCast,
                                  CardEffect afterSuccessfulCastEffect, UUID sourcePermanentId) {
        UUID cardId = card.getId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        SplitCardCast.ExileCast exileCast = new SplitCardCast.ExileCast(copy,
                putOnLibraryBottomInsteadOfGraveyard
                        || castingPermissionService.putsExileCastOnBottomOfOwnersLibrary(gameData, controllerId, cardId),
                payManaCost && castingPermissionService.hasExilePlayForLifeEqualToManaValuePermission(
                        gameData, controllerId, cardId),
                lifeLossOnCast);
        SplitCardCast cast = new SplitCardCast(controllerId, card, Zone.EXILE, 0,
                exiledEntry == null ? controllerId : exiledEntry.ownerId(),
                gameData.exileInsteadOfGraveyard.contains(cardId), Integer.MAX_VALUE,
                afterSuccessfulCastEffect, sourcePermanentId, payManaCost,
                castingPermissionService.hasAnyManaTypePermission(gameData, controllerId, cardId),
                0, false, null, exileCast);
        if (exiledEntry == null || !castingPermissionService.isSpellCastingAllowed(gameData, controllerId, card)) {
            returnToSourceZone(gameData, cast);
            return false;
        }
        return begin(gameData, cast);
    }

    /** Applies the chosen half, then asks for its targets or puts it onto the stack. */
    public void handleHalfChoice(GameData gameData, Player player, String chosenLabel,
                                 ChoiceContext.SplitFreeCastHalfChoice context) {
        SplitCardCast cast = context.cast();
        if (!player.getId().equals(cast.controllerId())) {
            throw new IllegalStateException("Not your choice");
        }
        Integer modeIndex = context.offeredModeIndices().stream()
                .filter(index -> context.effect().options().get(index).label().equals(chosenLabel))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid half: " + chosenLabel));
        gameData.interaction.clearAwaitingInput();

        ExileFreeCastQueueSupport.PreparedModalCast prepared = prepare(gameData, cast, context.effect(), modeIndex);
        gameLogService.append(gameData, GameLog.textCardText(
                player.getUsername() + " chooses \"" + chosenLabel + "\" for ", cast.physicalCard(), "."));
        SplitCardCast.Targeting targeting = new SplitCardCast.Targeting(cast, prepared.card(),
                exileCastTargetSupport.mapCardTypeToSpellType(prepared.card()), prepared.effects(),
                prepared.effectiveXValue(), modeEncoding(context.effect(), modeIndex), List.of());
        if (!needsTargets(prepared.card(), prepared.effects())) {
            putOnStack(gameData, targeting);
            return;
        }
        continueTargeting(gameData, targeting);
    }

    /** Records one chosen target, then asks for the next one or casts the spell. */
    public void handleTarget(GameData gameData, UUID targetId, PermanentChoiceContext.SplitFreeCastTarget context) {
        SplitCardCast.Targeting targeting = context.targeting();
        List<UUID> chosen = new ArrayList<>(targeting.chosenTargets());
        chosen.add(targetId);
        continueTargeting(gameData, new SplitCardCast.Targeting(targeting.cast(), targeting.cardToCast(),
                targeting.spellType(), targeting.spellEffects(), targeting.xValue(), targeting.modeEncoding(),
                chosen));
    }

    /** Applies the choice between taking another optional target and casting the spell now. */
    public void handleMoreTargetsChoice(GameData gameData, Player player, String choice,
                                        ChoiceContext.SplitFreeCastMoreTargetsChoice context) {
        SplitCardCast.Targeting targeting = context.targeting();
        if (!player.getId().equals(targeting.cast().controllerId())) {
            throw new IllegalStateException("Not your choice");
        }
        gameData.interaction.clearAwaitingInput();
        if (DONE.equals(choice)) {
            putOnStack(gameData, targeting);
        } else if (CHOOSE_TARGET.equals(choice)) {
            beginTargetChoice(gameData, targeting, context.candidates());
        } else {
            throw new IllegalArgumentException("Invalid choice: " + choice);
        }
    }

    /**
     * The modes of a split card's modal spell (its halves, then its fused spell) whose spell has the
     * characteristics {@code test} asks for. A permission to cast a spell with certain
     * characteristics considers only the half being cast (CR 709.3a); check while the card is still
     * in its zone, so counts such as the cards in hand are those the permission sees.
     */
    public List<Integer> modesMatching(GameData gameData, Card card, UUID controllerId, Predicate<Card> test) {
        ChooseOneEffect modal = splitModal(card);
        List<Integer> matching = new ArrayList<>();
        for (int modeIndex = 0; modeIndex < modal.options().size(); modeIndex++) {
            try {
                Card spell = exileFreeCastQueueSupport.prepareModalCast(
                        gameData, card, controllerId, modal, List.of(modeIndex)).card();
                if (test.test(spell)) {
                    matching.add(modeIndex);
                }
            } catch (IllegalStateException | IllegalArgumentException ignored) {
                // A mode that can't be prepared from here can't be cast from here either.
            }
        }
        return matching;
    }

    private boolean isCastableMode(GameData gameData, SplitCardCast cast, ChooseOneEffect modal, int modeIndex) {
        Card card = cast.physicalCard();
        if (cast.allowedModes() != null && !cast.allowedModes().contains(modeIndex)) {
            return false;
        }
        boolean fused = isFusedMode(card, modeIndex);
        boolean fromOwnHand = cast.sourceZone() == Zone.HAND && cast.ownerId().equals(cast.controllerId());
        if (fused && (!fromOwnHand || !card.hasKeyword(Keyword.FUSE))) {
            return false;
        }
        try {
            ExileFreeCastQueueSupport.PreparedModalCast prepared = prepare(gameData, cast, modal, modeIndex);
            if (prepared.card().getManaValue() > cast.maxManaValue()) {
                return false;
            }
            return !needsTargets(prepared.card(), prepared.effects())
                    || hasLegalTargets(gameData, cast.controllerId(), prepared, modeEncoding(modal, modeIndex));
        } catch (IllegalStateException | IllegalArgumentException ignored) {
            return false;
        }
    }

    private ExileFreeCastQueueSupport.PreparedModalCast prepare(GameData gameData, SplitCardCast cast,
                                                                ChooseOneEffect modal, int modeIndex) {
        return exileFreeCastQueueSupport.prepareModalCast(
                gameData, cast.physicalCard(), cast.controllerId(), modal, List.of(modeIndex));
    }

    private boolean hasLegalTargets(GameData gameData, UUID controllerId,
                                    ExileFreeCastQueueSupport.PreparedModalCast prepared, int modeEncoding) {
        Card card = prepared.card();
        int minTargets = hasTargetSlots(card) ? card.getMinTargets() : 1;
        List<UUID> chosen = new ArrayList<>();
        for (int slot = 0; slot < minTargets; slot++) {
            List<UUID> candidates = slotCandidates(
                    gameData, card, prepared.effects(), controllerId, chosen, modeEncoding);
            if (candidates.isEmpty()) {
                return false;
            }
            chosen.add(candidates.getFirst());
        }
        return true;
    }

    private void continueTargeting(GameData gameData, SplitCardCast.Targeting targeting) {
        Card card = targeting.cardToCast();
        UUID controllerId = targeting.cast().controllerId();
        int chosenCount = targeting.chosenTargets().size();
        int minTargets = hasTargetSlots(card) ? card.getMinTargets() : 1;
        int maxTargets = hasTargetSlots(card) ? card.getMaxTargets() : 1;
        if (chosenCount >= maxTargets) {
            putOnStack(gameData, targeting);
            return;
        }

        List<UUID> candidates = slotCandidates(gameData, card, targeting.spellEffects(), controllerId,
                targeting.chosenTargets(), targeting.modeEncoding());
        if (candidates.isEmpty()) {
            if (chosenCount >= minTargets) {
                putOnStack(gameData, targeting);
            } else {
                // Every half offered had a legal set of targets, so this happens only if a remaining
                // target vanished while the earlier ones were chosen.
                returnToSourceZone(gameData, targeting.cast());
                gameLogService.append(gameData, GameLog.cardThen(targeting.cast().physicalCard(),
                        " has no legal targets and can't be cast."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            }
            return;
        }

        if (chosenCount >= minTargets) {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new ChoiceContext.SplitFreeCastMoreTargetsChoice(targeting, candidates),
                    List.of(CHOOSE_TARGET, DONE), "Choose another target for " + card.getName() + "?"));
            return;
        }
        beginTargetChoice(gameData, targeting, candidates);
    }

    /**
     * Legal candidates for the next target of the prepared half. A target group declared without a
     * filter (Fire's "one or two targets") takes whatever its effects can target.
     */
    private List<UUID> slotCandidates(GameData gameData, Card card, List<CardEffect> spellEffects,
                                      UUID controllerId, List<UUID> chosenTargets, int modeEncoding) {
        if (!hasTargetSlots(card)) {
            return exileCastTargetSupport.effectTargetCandidates(gameData, card, spellEffects, controllerId);
        }
        if (card.getMultiTargetFilters().get(chosenTargets.size()) == null) {
            List<UUID> candidates = new ArrayList<>(
                    exileCastTargetSupport.effectTargetCandidates(gameData, card, spellEffects, controllerId));
            if (!card.isAllowSharedTargets()) {
                candidates.removeAll(chosenTargets);
            }
            return candidates;
        }
        return exileCastTargetSupport.nextSlotCandidates(gameData, card, controllerId, chosenTargets, modeEncoding);
    }

    private void beginTargetChoice(GameData gameData, SplitCardCast.Targeting targeting, List<UUID> candidates) {
        playerInputService.beginPermanentChoice(gameData, targeting.cast().controllerId(), candidates,
                new PermanentChoiceContext.SplitFreeCastTarget(targeting),
                "Choose a target for " + targeting.cardToCast().getName() + ".");
    }

    private void putOnStack(GameData gameData, SplitCardCast.Targeting targeting) {
        SplitCardCast cast = targeting.cast();
        Card card = targeting.cardToCast();
        UUID controllerId = cast.controllerId();
        List<UUID> targets = targeting.chosenTargets();
        List<CardEffect> effects = new ArrayList<>(targeting.spellEffects());
        SplitCardCast.ExileCast exileCast = cast.exileCast();
        ExiledCardEntry exiledEntry = null;
        if (cast.sourceZone() == Zone.EXILE) {
            exiledEntry = gameData.findExiledCard(cast.physicalCard().getId());
            if (exiledEntry == null) {
                gameLogService.append(gameData, GameLog.cardThen(cast.physicalCard(),
                        " is no longer in exile and can't be cast."));
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }

        StackEntry entry;
        Map<UUID, Integer> damageAssignments = dividedDamageAssignments(gameData, targeting);
        if (damageAssignments != null) {
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), (UUID) null, damageAssignments);
        } else if (targets.size() == 1 && !hasTargetSlots(card)) {
            UUID targetId = targets.getFirst();
            Zone targetZone = gameQueryService.findCardInGraveyardById(gameData, targetId) != null ? Zone.GRAVEYARD
                    : gameData.stack.stream().anyMatch(stacked -> targetId.equals(stacked.getTargetableId()))
                    ? Zone.STACK : null;
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), targetId, null, null, targetZone, null, null);
        } else if (!targets.isEmpty()) {
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), targets);
        } else {
            entry = new StackEntry(targeting.spellType(), card, controllerId, card.getName(), effects,
                    targeting.xValue(), (UUID) null, null);
        }
        entry.setExileInsteadOfGraveyard(cast.exileInsteadOfGraveyard());
        entry.setSourceZone(cast.sourceZone());
        if (cast.sourceZone() == Zone.GRAVEYARD
                || (cast.sourceZone() == Zone.EXILE && !cast.ownerId().equals(controllerId))) {
            entry.setOwnerIdOverride(cast.ownerId());
        }
        if (exileCast != null) {
            entry.setCopy(exileCast.copy());
            entry.setCastCopy(exileCast.copy());
            entry.setPutOnBottomOfOwnersLibraryInsteadOfGraveyard(exileCast.putOnLibraryBottomInsteadOfGraveyard());
        }
        if (cast.payManaCost()) {
            try {
                if (cast.sourceZone() == Zone.EXILE) {
                    spellCastingService.payResolutionExileCastCost(gameData, controllerId, card,
                            targeting.xValue(), cast.anyManaType(),
                            exileCast != null && exileCast.payLifeEqualToManaValue());
                } else {
                    spellCastingService.paySpellManaCostFromNonHandZone(gameData, controllerId, card,
                            targeting.xValue(), cast.sourceZone(), cast.anyManaType());
                }
            } catch (IllegalStateException | IllegalArgumentException unpaid) {
                returnToSourceZone(gameData, cast);
                gameLogService.append(gameData, GameLog.cardThen(cast.physicalCard(),
                        " can't be cast because its mana cost can't be paid."));
                copySupport.flushDeferredGraveyardCastCopies(gameData);
                inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }
        }
        leaveSourceZone(gameData, cast);
        if (exileFreeCastQueueSupport.beginSacrificeCostIfNeeded(gameData, entry)) {
            return;
        }
        gameData.stack.add(entry);
        if (cast.restrictAdditionalSpellsThisTurn()) {
            gameData.preventAdditionalSpellCastsThisTurn(controllerId);
        }
        if (cast.sourceZone() == Zone.GRAVEYARD) {
            copySupport.copyGraveyardCastSpell(gameData, entry, card, controllerId, cast.copyCount());
        }
        gameData.recordSpellCast(controllerId, card);
        gameData.priorityPassedBy.clear();

        String playerName = gameData.playerIdToName.get(controllerId);
        String costPhrase = cast.payManaCost() ? "" : " without paying its mana cost";
        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card, costPhrase + "."));
        log.info("Game {} - {} casts {} from {}{}", gameData.id, playerName, card.getName(),
                cast.sourceZone(), costPhrase);

        UUID exiledSourcePermanentId = exiledEntry == null || exileCast == null || exileCast.copy()
                ? null : exiledEntry.sourcePermanentId();
        triggerCollectionService.checkSpellCastTriggers(gameData, card, controllerId, cast.sourceZone(),
                exiledSourcePermanentId);
        if (!targets.isEmpty()) {
            triggerCollectionService.checkBecomesTargetOfSpellTriggers(gameData);
        }
        exileCastTargetSupport.queueAfterSuccessfulCast(gameData, card, controllerId,
                cast.sourcePermanentId(), cast.afterSuccessfulCastEffect());
        if (exileCast != null && exileCast.lifeLossOnCast() > 0) {
            lifeSupport.applyLifeLoss(gameData, controllerId, exileCast.lifeLossOnCast(), card.getName());
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * The division of a "divided as you choose" damage half among its targets, announced as it is
     * cast (CR 601.2d), or null when the half divides no damage. Each target must be dealt at least
     * 1 damage, so the division is fixed with one target or with as many targets as damage.
     */
    private Map<UUID, Integer> dividedDamageAssignments(GameData gameData, SplitCardCast.Targeting targeting) {
        boolean dividesDamage = targeting.spellEffects().stream()
                .filter(DealDividedDamageEffect.class::isInstance)
                .map(DealDividedDamageEffect.class::cast)
                .anyMatch(effect -> effect.mode() == DivisionMode.CHOSEN && !effect.etbAssignments());
        List<UUID> targets = targeting.chosenTargets();
        if (!dividesDamage || targets.isEmpty()) {
            return null;
        }
        int total = dealDividedDamageSupport.damageAssignedToSingleTarget(gameData, targeting.spellEffects(),
                targeting.cast().controllerId(), targeting.xValue(), false);
        if (targets.size() != 1 && targets.size() != total) {
            throw new IllegalStateException("Choosing a damage division for " + targeting.cardToCast().getName()
                    + " is not supported");
        }
        Map<UUID, Integer> assignments = new LinkedHashMap<>();
        for (UUID target : targets) {
            assignments.put(target, targets.size() == 1 ? total : 1);
        }
        return assignments;
    }

    /**
     * Completes the card's move to the stack. The card was set aside while the half and its targets
     * were chosen, so a cast that fails leaves no trace; a card that leaves a graveyard is now
     * removed the usual way, so the game sees it leave. A card cast from exile stayed there and
     * leaves it now.
     */
    private void leaveSourceZone(GameData gameData, SplitCardCast cast) {
        UUID cardId = cast.physicalCard().getId();
        if (cast.sourceZone() == Zone.GRAVEYARD) {
            returnToSourceZone(gameData, cast);
            permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        } else if (cast.sourceZone() == Zone.EXILE) {
            gameData.clearDelayedActions(ReturnExiledCardToHandAtNextEndStep.class,
                    action -> action.cardId().equals(cardId));
            clearExileCastCostChanges(gameData, cardId);
            if (gameData.removeFromExile(cardId) && (cast.exileCast() == null || !cast.exileCast().copy())) {
                gameData.recordCardPlayedFromExile(cast.controllerId());
            }
        }
    }

    /**
     * Puts the card back where it was taken from. A card cast from exile never left it, but a copy
     * that isn't cast ceases to exist (CR 707.10a).
     */
    private void returnToSourceZone(GameData gameData, SplitCardCast cast) {
        if (cast.sourceZone() == Zone.EXILE) {
            UUID cardId = cast.physicalCard().getId();
            gameData.exilePlayCostModifiers.remove(cardId);
            if (cast.exileCast() != null && cast.exileCast().copy()) {
                clearExileCastCostChanges(gameData, cardId);
                gameData.removeFromExile(cardId);
            }
            return;
        }
        List<Card> zone = switch (cast.sourceZone()) {
            case HAND -> gameData.playerHands.computeIfAbsent(cast.ownerId(), ignored -> new ArrayList<>());
            case GRAVEYARD -> gameData.playerGraveyards.computeIfAbsent(cast.ownerId(), ignored -> new ArrayList<>());
            case LIBRARY -> gameData.playerDecks.computeIfAbsent(cast.ownerId(), ignored -> new ArrayList<>());
            default -> throw new IllegalStateException("Can't cast a split card from " + cast.sourceZone());
        };
        zone.add(Math.min(cast.sourceIndex(), zone.size()), cast.physicalCard());
    }

    /** Drops the cost changes installed for an exiled card once it leaves exile. */
    private static void clearExileCastCostChanges(GameData gameData, UUID cardId) {
        gameData.exilePlayCostModifiers.remove(cardId);
        gameData.exilePlayWithoutPayingManaCost.remove(cardId);
    }

    private static int modeEncoding(ChooseOneEffect modal, int modeIndex) {
        return ChooseOneEffect.encodeModeSelection(modal, modeIndex);
    }

    private static boolean needsTargets(Card card, List<CardEffect> spellEffects) {
        return EffectResolution.needsSpellCastTarget(spellEffects,
                card.isAuraThatRequiresAttachment(), card.isEnchantPlayer())
                || EffectResolution.needsSpellTarget(spellEffects);
    }

    /** Whether the half declares its own target slots, rather than a single cast-time target. */
    private static boolean hasTargetSlots(Card card) {
        return !card.getSpellTargets().isEmpty();
    }

    private static ChooseOneEffect splitModal(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance)
                .map(ChooseOneEffect.class::cast)
                .findFirst()
                .orElse(null);
    }
}
