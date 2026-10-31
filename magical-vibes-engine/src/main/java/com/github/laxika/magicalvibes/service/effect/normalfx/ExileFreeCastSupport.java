package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.SplitCardCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.FreeCastKickerOffer;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayKickerForFreeCastEffect;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.service.spell.SpellCastingService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.cast.CastingPermissionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Casts a real (non-copy) card that already sits in exile "without paying its mana cost", choosing a
 * target first when required. Used when a player is offered to play a card put into exile by an
 * effect such as Guile's counter-replacement. Timing/priority restrictions are ignored (the play is
 * part of another effect's resolution); if the spell can't be legally cast it stays exiled.
 */
@Slf4j
@Component
public class ExileFreeCastSupport {

    private final GameLogService gameLogService;
    private final AdditionalSpellCostService additionalSpellCostService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final InputCompletionService inputCompletionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    @org.springframework.beans.factory.annotation.Autowired @Lazy
    private CastingPermissionService castingPermissionService;

    @org.springframework.beans.factory.annotation.Autowired @Lazy
    private SpellCastingService spellCastingService;
    @org.springframework.beans.factory.annotation.Autowired @Lazy
    private ExileFreeCastQueueSupport exileFreeCastQueueSupport;
    @org.springframework.beans.factory.annotation.Autowired @Lazy
    private SplitCardCastSupport splitCardCastSupport;

    // @Lazy mirrors ParadigmCastSupport: breaks cycles through InputCompletionService/PlayerInputService.
    public ExileFreeCastSupport(GameLogService gameLogService,
                                @Lazy PlayerInputService playerInputService,
                                @Lazy TriggerCollectionService triggerCollectionService,
                                @Lazy InputCompletionService inputCompletionService,
                                ExileCastTargetSupport exileCastTargetSupport,
                                AdditionalSpellCostService additionalSpellCostService,
                                InteractionHandlerRegistry interactionHandlerRegistry) {
        this.gameLogService = gameLogService;
        this.additionalSpellCostService = additionalSpellCostService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.inputCompletionService = inputCompletionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.interactionHandlerRegistry = interactionHandlerRegistry;
    }

    public void castFromExileWithoutPaying(GameData gameData, Player player, UUID exileCardId) {
        castFromExileWithoutPaying(gameData, player, exileCardId, false);
    }

    public void castFromExileWithoutPaying(GameData gameData, Player player, UUID exileCardId,
                                           boolean grantHaste) {
        castFromExileWithoutPaying(gameData, player, exileCardId, grantHaste, false);
    }

    public void castFromExileWithoutPaying(GameData gameData, Player player, UUID exileCardId,
                                           boolean grantHaste, boolean returnToHandIfUnable) {
        castFromExileWithoutPaying(gameData, player, exileCardId, grantHaste, returnToHandIfUnable, false);
    }

    public void castFromExileWithoutPaying(GameData gameData, Player player, UUID exileCardId,
                                           boolean grantHaste, boolean returnToHandIfUnable, boolean suspendHaste) {
        castFromExileWithoutPaying(gameData, player, exileCardId, grantHaste, returnToHandIfUnable,
                suspendHaste, true);
    }

    /** Defers input completion when casting inside an effect resolution already in progress. */
    public void castFromExileWithoutPaying(GameData gameData, Player player, UUID exileCardId,
                                           boolean grantHaste, boolean returnToHandIfUnable, boolean suspendHaste,
                                           boolean completeInput) {
        ExiledCardEntry exiled = gameData.findExiledCard(exileCardId);
        if (exiled != null && exiled.card().getBackFaceCard() != null
                && exiled.card().getCastingOption(AdventureCast.class).isPresent()) {
            // An Adventure card may be cast as either face (CR 715.3), so the caster picks the creature or the Adventure.
            java.util.Map<String, Integer> faces = new java.util.LinkedHashMap<>();
            faces.put("Cast " + exiled.card().getName(), 0);
            faces.put("Cast " + exiled.card().getBackFaceCard().getName(), 1);
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    player.getId(), null, null,
                    new ChoiceContext.ExileFreeCastFaceChoice(exileCardId, faces, grantHaste,
                            returnToHandIfUnable, suspendHaste, completeInput),
                    new ArrayList<>(faces.keySet()), "Choose which spell to cast."));
            return;
        }
        castSelectedFace(gameData, player, exileCardId, grantHaste, returnToHandIfUnable,
                suspendHaste, completeInput, 0);
    }

    /** Offers every castable Adventure face that satisfies the effect's mana-value restriction. */
    public void castWithManaValueLimit(GameData gameData, Player player, UUID exileCardId, Integer maxManaValue) {
        castWithManaValueLimit(gameData, player, exileCardId, maxManaValue, true);
    }

    /** As above; {@code returnToHandIfUnable} false leaves a card that can't be cast in exile. */
    public void castWithManaValueLimit(GameData gameData, Player player, UUID exileCardId, Integer maxManaValue,
                                       boolean returnToHandIfUnable) {
        ExiledCardEntry exiled = gameData.findExiledCard(exileCardId);
        if (exiled == null || maxManaValue == null) {
            castFromExileWithoutPaying(gameData, player, exileCardId, false, returnToHandIfUnable);
            return;
        }
        Card card = exiled.card();
        if (SplitCardCastSupport.isSplitCard(card)) {
            castSplitCard(gameData, player, card, true, true, maxManaValue);
            return;
        }
        java.util.Map<String, Integer> faces = new java.util.LinkedHashMap<>();
        if (!card.hasType(CardType.LAND) && card.getManaValue() <= maxManaValue) {
            faces.put("Cast " + card.getName(), 0);
        }
        if (card.getCastingOption(AdventureCast.class).isPresent() && card.getBackFaceCard() != null
                && card.getBackFaceCard().getManaValue() <= maxManaValue) {
            faces.put("Cast " + card.getBackFaceCard().getName(), 1);
        }
        if (faces.isEmpty()) {
            if (returnToHandIfUnable) returnExiledCardToHand(gameData, exileCardId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } else if (faces.size() == 1) {
            castSelectedFace(gameData, player, exileCardId, false, returnToHandIfUnable, false, true,
                    faces.values().iterator().next());
        } else {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    player.getId(), null, null,
                    new ChoiceContext.ExileFreeCastFaceChoice(exileCardId, faces, false, returnToHandIfUnable,
                            false, true),
                    new ArrayList<>(faces.keySet()), "Choose which spell to cast."));
        }
    }

    public void completeFaceChoice(GameData gameData, Player player, String choice,
                                   ChoiceContext.ExileFreeCastFaceChoice context) {
        Integer face = context.faces().get(choice);
        if (face == null) throw new IllegalStateException("Invalid spell face choice");
        gameData.interaction.clearAwaitingInput();
        castSelectedFace(gameData, player, context.exileCardId(), context.grantHaste(),
                context.returnToHandIfUnable(), context.suspendHaste(), context.completeInput(), face);
    }

    private void castSelectedFace(GameData gameData, Player player, UUID exileCardId,
                                  boolean grantHaste, boolean returnToHandIfUnable, boolean suspendHaste,
                                  boolean completeInput, int face) {
        UUID playerId = player.getId();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null) {
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        Card physicalCard = exiledEntry.card();
        if (suspendHaste && face == 0 && physicalCard.hasType(CardType.LAND)) {
            // CR 702.62a: when the last time counter is removed the controller may *play* the card, so a
            // suspended land (e.g. an animated manland given suspend) is played as a land if able;
            // otherwise it remains exiled.
            if (!spellCastingService.playLandFromExileDuringResolution(gameData, player, exileCardId)) {
                gameLogService.append(gameData, GameLog.cardThen(physicalCard,
                        " cannot be played and stays exiled."));
            }
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (face == 0 && SplitCardCastSupport.isSplitCard(physicalCard)) {
            castSplitCard(gameData, player, physicalCard, returnToHandIfUnable, completeInput, Integer.MAX_VALUE);
            return;
        }
        Card card = face == 1 ? physicalCard.createRuntimeCopyWithFace(physicalCard.getBackFaceCard()) : physicalCard;
        ChooseOneEffect splitModes = card.getSpellEffects().stream()
                .filter(ChooseOneEffect.class::isInstance).map(ChooseOneEffect.class::cast)
                .filter(modal -> modal.options().stream().anyMatch(option -> option.manaCost() != null)
                        || (modal.choicesRequired() == 1 && !modal.modesMayRepeat()))
                .findFirst().orElse(null);
        List<CardEffect> chosenEffects = null;
        if (splitModes != null) {
            if (face < 2) {
                java.util.Map<String, Integer> modes = new java.util.LinkedHashMap<>();
                for (int mode = 0; mode < splitModes.options().size(); mode++) {
                    if (splitModes.options().get(mode).handOnly()) continue;
                    modes.put(splitModes.options().get(mode).label(), mode + 2);
                }
                interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                        playerId, null, null,
                        new ChoiceContext.ExileFreeCastFaceChoice(exileCardId, modes, grantHaste,
                                returnToHandIfUnable, suspendHaste, completeInput),
                        new ArrayList<>(modes.keySet()), "Choose which spell to cast."));
                return;
            }
            card = card.createRuntimeCopy();
            chosenEffects = new ArrayList<>(card.getCastingCostsAndSpellEffects());
            spellCastingService.prepareModalSpellCast(gameData, playerId, card, chosenEffects, face - 2);
        }
        boolean exileInsteadOfGraveyard = gameData.exileInsteadOfGraveyard.contains(exileCardId);
        if (!castingPermissionService.canCastDuringResolution(gameData, playerId)) {
            if (returnToHandIfUnable) returnExiledCardToHand(gameData, exileCardId);
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (!castingPermissionService.canCastWithSpellTimingRestriction(gameData, playerId, card)
                || !castingPermissionService.canCastWithCastCondition(gameData, playerId, card)) {
            // "Cast this spell only ..." is a "can't" restriction that still applies to a free cast (CR 601.3,
            // CR 101.2), so a spell that can't legally be cast right now stays where it was.
            if (returnToHandIfUnable) returnExiledCardToHand(gameData, exileCardId);
            gameLogService.append(gameData, GameLog.cardThen(card, " cannot be cast right now."));
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (card.isCastOnlyFromGraveyard()) {
            if (returnToHandIfUnable) {
                returnExiledCardToHand(gameData, exileCardId);
                gameLogService.append(gameData,
                        GameLog.cardThen(card, " cannot be cast from exile and is put into its owner's hand."));
            } else {
                gameLogService.append(gameData, GameLog.cardThen(card, " cannot be cast from exile and stays exiled."));
            }
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (!additionalSpellCostService.satisfiableWithoutManaCost(gameData, playerId, card)) {
            if (returnToHandIfUnable) {
                returnExiledCardToHand(gameData, exileCardId);
            }
            gameLogService.append(gameData, GameLog.cardThen(card,
                    " cannot be cast because its additional cost cannot be paid."));
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (offerKickerForFreeCast(gameData, player, card, exileCardId,
                new FreeCastKickerOffer(grantHaste, returnToHandIfUnable, suspendHaste, completeInput, face))) {
            return;
        }
        String playerName = player.getUsername();
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = chosenEffects != null ? chosenEffects
                : new ArrayList<>(card.getCastingCostsAndSpellEffects());

        // A spell with no declared target groups (auto-derived targeting) cannot be cast without a target.
        // A modal spell's targets belong to the chosen mode, whose effects were unwrapped above.
        boolean needsTarget = chosenEffects != null
                ? EffectResolution.needsTarget(spellEffects, List.of(), card.isAuraThatRequiresAttachment(),
                card.isEnchantPlayer())
                : EffectResolution.needsTarget(card) || (!card.getTargetGroups().isEmpty()
                && !EffectResolution.targetsOnlyWithEtbAbilities(card));
        List<UUID> firstCandidates = !needsTarget ? List.of()
                : chosenEffects != null
                ? exileCastTargetSupport.firstSlotCandidates(gameData, card, spellEffects, playerId)
                : exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId);
        if (needsTarget && (card.getMinTargets() > 0 || card.getMaxTargets() == 0
                || !firstCandidates.isEmpty())) {
            boolean multiTarget = card.getMaxTargets() > 1;
            boolean hasLegalTargets = multiTarget
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, playerId)
                    : !firstCandidates.isEmpty();

            if (!hasLegalTargets) {
                gameData.spellsGrantedHasteOnEntry.remove(exileCardId);
                gameData.spellsGrantedSuspendHasteOnEntry.remove(exileCardId);
                if (returnToHandIfUnable) {
                    returnExiledCardToHand(gameData, exileCardId);
                    gameLogService.append(gameData,
                            GameLog.cardThen(card, " has no valid targets and is put into its owner's hand."));
                } else {
                    gameLogService.append(gameData, GameLog.cardThen(card, " has no valid targets and stays exiled."));
                }
                log.info("Game {} - {} exile free-cast has no valid targets", gameData.id, card.getName());
                if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
                return;
            }

            // Remove from exile now that it will be cast; the ExileCastSpellTarget flow puts it on the stack.
            gameData.freeCastKickerDecidedIds.remove(exileCardId);
            gameData.removeFromExile(exileCardId);
            if (exileInsteadOfGraveyard) {
                gameData.exileInsteadOfGraveyard.add(exileCardId);
            }
            if (grantHaste && card.hasType(CardType.CREATURE)) {
                gameData.spellsGrantedHasteOnEntry.add(exileCardId);
            }
            if (suspendHaste && card.hasType(CardType.CREATURE)) {
                gameData.spellsGrantedSuspendHasteOnEntry.add(exileCardId);
            }
            gameData.recordCardPlayedFromExile(playerId);
            gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ExileCastSpellTarget(
                    card, playerId, spellEffects, spellType, false, List.of(), 0, false, 0,
                    false, false, null, exiledEntry.sourcePermanentId(), 0, physicalCard));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + card.getName() + ".");

            gameLogService.append(gameData, GameLog.playerPlays(playerName, card,
                    " without paying its mana cost — choosing target."));
            return;
        }

        if (EffectResolution.needsSpellTarget(spellEffects)
                && exileCastTargetSupport.flatSingleTargetCandidates(gameData, card, playerId).isEmpty()) {
            gameData.spellsGrantedHasteOnEntry.remove(exileCardId);
            gameData.spellsGrantedSuspendHasteOnEntry.remove(exileCardId);
            if (returnToHandIfUnable) {
                returnExiledCardToHand(gameData, exileCardId);
                gameLogService.append(gameData,
                        GameLog.cardThen(card, " has no valid targets and is put into its owner's hand."));
            } else {
                gameLogService.append(gameData, GameLog.cardThen(card, " has no valid targets and stays exiled."));
            }
            log.info("Game {} - {} exile free-cast has no spell to target", gameData.id, card.getName());
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.removeFromExile(exileCardId);
        StackEntry stackEntry = new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        );
        stackEntry.setExileInsteadOfGraveyard(exileInsteadOfGraveyard);
        stackEntry.setPhysicalCard(physicalCard);
        stackEntry.setCastWithAdventure(face == 1);
        stackEntry.setOwnerIdOverride(exiledEntry.ownerId());
        stackEntry.setSourceZone(Zone.EXILE);
        stackEntry.setSuspendHasteOnEntry(suspendHaste && card.hasType(CardType.CREATURE));
        stackEntry.setKicked(gameData.freeCastKickedIds.remove(exileCardId));
        gameData.freeCastKickerDecidedIds.remove(exileCardId);
        if (grantHaste && card.hasType(CardType.CREATURE)) {
            gameData.spellsGrantedHasteOnEntry.add(exileCardId);
        }
        gameData.recordCardPlayedFromExile(playerId);
        if (exileFreeCastQueueSupport.beginSacrificeCostIfNeeded(gameData, stackEntry)) return;
        gameData.stack.add(stackEntry);

        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();

        gameLogService.append(gameData,
                GameLog.playerPlays(playerName, card, " without paying its mana cost."));
        log.info("Game {} - {} plays {} from exile without paying mana", gameData.id, playerName, card.getName());

        triggerCollectionService.checkSpellCastTriggers(
                gameData, card, playerId, Zone.EXILE, exiledEntry.sourcePermanentId());
        if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * Kicker is an optional additional cost, so a spell cast without paying its mana cost may still be
     * kicked (CR 118.9, CR 601.2b). Offers a mana-only kicker the controller can currently afford and
     * pauses the cast until that offer is answered.
     *
     * @return true when the cast is paused on the offer
     */
    private boolean offerKickerForFreeCast(GameData gameData, Player player, Card card, UUID exileCardId,
                                           FreeCastKickerOffer offer) {
        if (!offer.completeInput() || gameData.freeCastKickerDecidedIds.contains(exileCardId)) {
            return false;
        }
        KickerEffect kicker = card.getStaticEffects().stream()
                .filter(KickerEffect.class::isInstance)
                .map(KickerEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (kicker == null || !kicker.hasManaCost() || kicker.hasSacrificeCost() || kicker.hasTapCost()
                || kicker.hasReturnCost() || kicker.hasDiscardCost() || kicker.hasLifeCost()
                || kicker.hasForageCost()) {
            return false;
        }
        ManaCost cost = new ManaCost(kicker.cost());
        ManaPool pool = gameData.playerManaPools.get(player.getId());
        if (cost.hasX() || pool == null || !cost.canPay(pool, 0)) {
            return false;
        }
        gameData.pendingFreeCastKickerOffers.put(exileCardId, offer);
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(card, player.getId(),
                List.of(new MayPayKickerForFreeCastEffect()),
                "Pay the kicker cost " + kicker.cost() + " for " + card.getName() + "?", exileCardId));
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        return true;
    }

    /** Resumes a free cast that paused for its kicker offer, with the options it was started with. */
    public void resumeAfterKickerDecision(GameData gameData, Player player, UUID exileCardId,
                                          FreeCastKickerOffer offer) {
        castSelectedFace(gameData, player, exileCardId, offer.grantHaste(), offer.returnToHandIfUnable(),
                offer.suspendHaste(), offer.completeInput(), offer.face());
    }

    /**
     * Casts an exiled split card as one half, chosen with its targets before it is put onto the stack
     * (CR 709.3); only a half whose mana value is at most {@code maxManaValue} is offered (CR 709.3a).
     */
    private void castSplitCard(GameData gameData, Player player, Card card, boolean returnToHandIfUnable,
                               boolean completeInput, int maxManaValue) {
        SplitCardCast.ExileCast exileCast = returnToHandIfUnable
                ? SplitCardCast.ExileCast.ofCard().puttingIntoHandIfNotCast()
                : SplitCardCast.ExileCast.ofCard();
        if (!castingPermissionService.canCastDuringResolution(gameData, player.getId())) {
            if (returnToHandIfUnable) returnExiledCardToHand(gameData, card.getId());
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }
        if (!splitCardCastSupport.beginFromExile(gameData, player.getId(), card, false, exileCast, maxManaValue,
                null, null)) {
            if (!returnToHandIfUnable) {
                gameLogService.append(gameData, GameLog.cardThen(card,
                        " has no half that can be cast and stays exiled."));
            }
            if (completeInput) inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        }
    }

    public boolean returnExiledCardToHand(GameData gameData, UUID exileCardId) {
        ExiledCardEntry exiledEntry = gameData.findExiledCard(exileCardId);
        if (exiledEntry == null) {
            return false;
        }
        gameData.removeFromExile(exileCardId);
        gameData.addCardToHand(exiledEntry.ownerId(), exiledEntry.card());
        return true;
    }
}
