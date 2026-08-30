package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Casts a queue of exiled cards "without paying their mana costs" one at a time, so a cast that
 * pauses for target selection can resume the remaining ones (Improvisation Capstone, Hazoret's
 * Undying Fury, Brilliant Ultimatum, Spelltwine's two copies).
 *
 * <p>Entries queued through {@link #queueCopiesForFreeCast} are copies rather than real cards: they
 * are cast as copies and cease to exist instead of being put into a zone.</p>
 */
@Slf4j
@Component
public class ExileFreeCastQueueSupport {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileCastTargetSupport exileCastTargetSupport;
    private final InputCompletionService inputCompletionService;
    private final GraveyardService graveyardService;
    private final CopySupport copySupport;
    private final SpellweaverVoluteSupport spellweaverVoluteSupport;

    // @Lazy mirrors ExileFreeCastSupport: breaks the cycle back through the input services.
    public ExileFreeCastQueueSupport(GameLogService gameLogService,
                                            PlayerInputService playerInputService,
                                            TriggerCollectionService triggerCollectionService,
                                            ExileCastTargetSupport exileCastTargetSupport,
                                            @Lazy InputCompletionService inputCompletionService,
                                            GraveyardService graveyardService,
                                            CopySupport copySupport,
                                            @Lazy SpellweaverVoluteSupport spellweaverVoluteSupport) {
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.inputCompletionService = inputCompletionService;
        this.graveyardService = graveyardService;
        this.copySupport = copySupport;
        this.spellweaverVoluteSupport = spellweaverVoluteSupport;
    }

    public void castChosenSpellsWithoutPaying(GameData gameData, Player player, List<UUID> cardIds) {
        gameData.interaction.clearAwaitingInput();

        if (cardIds == null || cardIds.isEmpty()) {
            String logEntry = player.getUsername() + " casts no spells for free.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            finishFreeCastProcess(gameData);
            return;
        }

        gameData.pendingFreeCastQueue.clear();
        gameData.pendingFreeCastQueue.addAll(cardIds);
        castNextFromQueue(gameData, player.getId());
    }

    /** Casts the selected Eye of the Storm copies in the order chosen by their caster. */
    public void castChosenCopiesWithoutPaying(GameData gameData, UUID playerId, List<UUID> copyIds) {
        gameData.interaction.clearAwaitingInput();
        queueCopiesForFreeCast(gameData, playerId, copyIds);
    }

    /**
     * Queues copies sitting in exile to be cast for free, in the given order, and starts the chain.
     * Each is cast as a copy, so it ceases to exist on resolution (CR 707.10a).
     */
    public void queueCopiesForFreeCast(GameData gameData, UUID playerId, List<UUID> copyIds) {
        gameData.pendingFreeCastQueue.addAll(copyIds);
        gameData.pendingFreeCastAsCopyIds.addAll(copyIds);
        castNextFromQueue(gameData, playerId);
    }

    /**
     * Moves every still-exiled card tracked in
     * {@link GameData#pendingExileFreeCastRemainderToGraveyard} to its owner's graveyard, then
     * clears the list. No-op when the list is empty (Improvisation Capstone / Hazoret's Undying Fury).
     */
    public void putRemainderIntoOwnersGraveyards(GameData gameData) {
        List<UUID> remainder = new ArrayList<>(gameData.pendingExileFreeCastRemainderToGraveyard);
        gameData.pendingExileFreeCastRemainderToGraveyard.clear();
        for (UUID cardId : remainder) {
            ExiledCardEntry entry = gameData.findExiledCard(cardId);
            if (entry == null) {
                continue;
            }
            gameData.removeFromExile(cardId);
            graveyardService.addCardToGraveyard(gameData, entry.ownerId(), entry.card());
            gameLogService.append(gameData, GameLog.cardThen(entry.card(), " is put into its owner's graveyard."));
        }
    }


    /**
     * Casts the next queued exiled spell. When a spell requires a target this pauses for a target
     * choice and returns; the shared target handler resumes the queue via {@link #castNextFromQueue}
     * once the target is chosen.
     *
     * <p>Once the queue drains this ends through the shared {@link InputCompletionService} epilogue,
     * which resumes the stack entry parked in {@code GameData.pendingEffectResolutionEntry} when the
     * cast choice was begun. Ending any other way leaves that entry dangling, which wedges
     * {@code GameData.deferPlayerLossCheck} so no player can ever lose to a state-based action again.
     */
    public void castNextFromQueue(GameData gameData, UUID playerId) {
        if (gameData.pendingFreeCastQueue.isEmpty()) {
            finishFreeCastProcess(gameData);
            return;
        }

        UUID cardId = gameData.pendingFreeCastQueue.removeFirst();
        boolean asCopy = gameData.pendingFreeCastAsCopyIds.remove(cardId);
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        if (exiledEntry == null) {
            castNextFromQueue(gameData, playerId);
            return;
        }

        Card card = exiledEntry.card();
        if (card.isCastOnlyFromGraveyard()) {
            if (asCopy) {
                gameData.removeFromExile(cardId);
            }
            gameLogService.append(gameData, GameLog.cardThen(card,
                    asCopy ? " cannot be cast from exile." : " cannot be cast from exile and stays exiled."));
            castNextFromQueue(gameData, playerId);
            return;
        }
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        Card cardToCast = asCopy
                && !card.hasType(CardType.INSTANT)
                && !card.hasType(CardType.SORCERY)
                ? copySupport.createTokenCopyCard(card)
                : card;
        List<CardEffect> spellEffects = new ArrayList<>(cardToCast.getEffects(EffectSlot.SPELL));
        String playerName = gameData.playerIdToName.get(playerId);

        if (EffectResolution.needsTarget(card)) {
            List<UUID> firstCandidates = exileCastTargetSupport.firstSlotCandidates(gameData, card, playerId);
            boolean multiTarget = card.getMaxTargets() > 1;
            // Multi-target: require a full legal set of targets (CR 601.2c) before prompting.
            boolean hasLegalTargets = multiTarget
                    ? exileCastTargetSupport.hasLegalTargetSet(gameData, card, playerId)
                    : !firstCandidates.isEmpty();

            if (!hasLegalTargets) {
                // A real card was never cast and nothing moves it elsewhere: it stays exiled. A copy
                // that can't be legally cast simply ceases to exist (CR 707.10a). Epic Experiment's
                // remainder list moves uncaught cards to the graveyard when the free-cast process finishes.
                if (asCopy) {
                    gameData.removeFromExile(cardId);
                }
                boolean willGoToGraveyard = gameData.pendingExileFreeCastRemainderToGraveyard.contains(cardId);
                gameLogService.append(gameData, GameLog.cardThen(card, willGoToGraveyard
                        ? " has no valid targets and will be put into the graveyard."
                        : asCopy ? " has no valid targets."
                        : " has no valid targets and stays exiled."));
                castNextFromQueue(gameData, playerId);
                return;
            }

            // Remove from exile now that it will be cast; the ExileCastSpellTarget flow puts it on the stack.
            gameData.removeFromExile(cardId);
            if (!asCopy) {
                gameData.recordCardPlayedFromExile(playerId);
            }
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(cardToCast, playerId, spellEffects, spellType, asCopy));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + card.getName() + ".");
            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card, " without paying its mana cost — choosing target."));
            return;
        }

        gameData.removeFromExile(cardId);
        if (!asCopy) {
            gameData.recordCardPlayedFromExile(playerId);
        }
        StackEntry entry = new StackEntry(
                spellType, cardToCast, playerId, cardToCast.getName(),
                spellEffects, 0, (UUID) null, null
        );
        entry.setCopy(asCopy);
        if (!asCopy) {
            entry.setSourceZone(Zone.EXILE);
        }
        gameData.stack.add(entry);
        gameData.recordSpellCast(playerId, cardToCast);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card, " without paying its mana cost."));
        triggerCollectionService.checkSpellCastTriggers(gameData, cardToCast, playerId, false);
        if (asCopy && spellweaverVoluteSupport.handleSuccessfulCopyCast(gameData, cardId)) {
            return;
        }
        castNextFromQueue(gameData, playerId);
    }

    /**
     * Ends the free-cast process. The shared input epilogue resumes the entry parked in
     * {@code GameData.pendingEffectResolutionEntry} — right when the queue was driven from an input
     * handler, wrong when it ran inside a live effect-resolution frame (a cipher trigger's "you may
     * cast a copy", CR 702.99a). That frame still sits on the parked index, so re-entering it re-runs
     * the same {@code MayEffect} and prompts a second time — casting a second copy if accepted. The
     * active frame completes the resolution itself, so skip the epilogue while one is running.
     */
    private void finishFreeCastProcess(GameData gameData) {
        spellweaverVoluteSupport.clearIfUncast(gameData);
        putRemainderIntoOwnersGraveyards(gameData);
        if (gameData.effectResolutionDepth > 0 && gameData.pendingEffectResolutionEntry != null) {
            return;
        }
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
