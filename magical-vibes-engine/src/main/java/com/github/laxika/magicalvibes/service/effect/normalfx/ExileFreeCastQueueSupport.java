package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
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

    // @Lazy mirrors ExileFreeCastSupport: breaks the cycle back through the input services.
    public ExileFreeCastQueueSupport(GameLogService gameLogService,
                                            PlayerInputService playerInputService,
                                            TriggerCollectionService triggerCollectionService,
                                            ExileCastTargetSupport exileCastTargetSupport,
                                            @Lazy InputCompletionService inputCompletionService) {
        this.gameLogService = gameLogService;
        this.playerInputService = playerInputService;
        this.triggerCollectionService = triggerCollectionService;
        this.exileCastTargetSupport = exileCastTargetSupport;
        this.inputCompletionService = inputCompletionService;
    }

    public void castChosenSpellsWithoutPaying(GameData gameData, Player player, List<UUID> cardIds) {
        gameData.interaction.clearAwaitingInput();

        if (cardIds == null || cardIds.isEmpty()) {
            String logEntry = player.getUsername() + " casts no spells from Improvisation Capstone.";
            gameLogService.append(gameData, GameLog.text(logEntry));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameData.pendingFreeCastQueue.clear();
        gameData.pendingFreeCastQueue.addAll(cardIds);
        castNextFromQueue(gameData, player.getId());
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
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
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
        StackEntryType spellType = exileCastTargetSupport.mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
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
                // that can't be legally cast simply ceases to exist (CR 707.10a).
                if (asCopy) {
                    gameData.removeFromExile(cardId);
                }
                gameLogService.append(gameData, GameLog.cardThen(card, " has no valid targets."));
                castNextFromQueue(gameData, playerId);
                return;
            }

            // Remove from exile now that it will be cast; the ExileCastSpellTarget flow puts it on the stack.
            gameData.removeFromExile(cardId);
            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(card, playerId, spellEffects, spellType, asCopy));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + card.getName() + ".");
            gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card, " without paying its mana cost — choosing target."));
            return;
        }

        gameData.removeFromExile(cardId);
        StackEntry entry = new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        );
        entry.setCopy(asCopy);
        gameData.stack.add(entry);
        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();
        gameLogService.append(gameData, GameLog.textCardText(playerName + " casts ", card, " without paying its mana cost."));
        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        castNextFromQueue(gameData, playerId);
    }
}
