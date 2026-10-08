package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.AdventureCast;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
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
        castSelectedFace(gameData, player, exileCardId, grantHaste, returnToHandIfUnable,
                suspendHaste, completeInput, 0);
    }

    /** Offers every castable Adventure face that satisfies the effect's mana-value restriction. */
    public void castWithManaValueLimit(GameData gameData, Player player, UUID exileCardId, Integer maxManaValue) {
        ExiledCardEntry exiled = gameData.findExiledCard(exileCardId);
        if (exiled == null || maxManaValue == null) {
            castFromExileWithoutPaying(gameData, player, exileCardId, false, true);
            return;
        }
        Card card = exiled.card();
        java.util.Map<String, Integer> faces = new java.util.LinkedHashMap<>();
        if (!card.hasType(CardType.LAND) && card.getManaValue() <= maxManaValue) {
            faces.put("Cast " + card.getName(), 0);
        }
        if (card.getCastingOption(AdventureCast.class).isPresent() && card.getBackFaceCard() != null
                && card.getBackFaceCard().getManaValue() <= maxManaValue) {
            faces.put("Cast " + card.getBackFaceCard().getName(), 1);
        }
        if (faces.isEmpty()) {
            returnExiledCardToHand(gameData, exileCardId);
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
        } else if (faces.size() == 1) {
            castSelectedFace(gameData, player, exileCardId, false, true, false, true,
                    faces.values().iterator().next());
        } else {
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    player.getId(), null, null,
                    new ChoiceContext.ExileFreeCastFaceChoice(exileCardId, faces, false, true, false, true),
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
        Card card = face == 1 ? physicalCard.createRuntimeCopyWithFace(physicalCard.getBackFaceCard()) : physicalCard;
        ChooseOneEffect splitModes = card.getEffects(EffectSlot.SPELL).stream()
                .filter(ChooseOneEffect.class::isInstance).map(ChooseOneEffect.class::cast)
                .filter(modal -> modal.options().stream().anyMatch(option -> option.manaCost() != null))
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
            chosenEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
            spellCastingService.prepareModalSpellCast(gameData, playerId, card, chosenEffects, face - 2);
        }
        boolean exileInsteadOfGraveyard = gameData.exileInsteadOfGraveyard.contains(exileCardId);
        if (!castingPermissionService.canCastDuringResolution(gameData, playerId)) {
            if (returnToHandIfUnable) returnExiledCardToHand(gameData, exileCardId);
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
        String playerName = player.getUsername();
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = chosenEffects != null ? chosenEffects
                : new ArrayList<>(card.getEffects(EffectSlot.SPELL));

        if (EffectResolution.needsTarget(card) && (card.getMinTargets() > 0
                || !exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId).isEmpty())) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId);
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
