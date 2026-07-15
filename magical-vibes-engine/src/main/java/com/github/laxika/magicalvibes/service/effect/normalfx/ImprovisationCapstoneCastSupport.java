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
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprovisationCapstoneCastSupport {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;
    private final ExileCastTargetSupport exileCastTargetSupport;

    public void castChosenSpellsWithoutPaying(GameData gameData, Player player, List<UUID> cardIds) {
        gameData.interaction.clearAwaitingInput();

        if (cardIds == null || cardIds.isEmpty()) {
            String logEntry = player.getUsername() + " casts no spells from Improvisation Capstone.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }

        gameData.pendingImprovisationCapstoneCastQueue.clear();
        gameData.pendingImprovisationCapstoneCastQueue.addAll(cardIds);
        castNextFromQueue(gameData, player.getId());
    }

    /**
     * Casts the next queued exiled spell. When a spell requires a target this pauses for a target
     * choice and returns; the shared target handler resumes the queue via {@link #castNextFromQueue}
     * once the target is chosen.
     */
    public void castNextFromQueue(GameData gameData, UUID playerId) {
        if (gameData.pendingImprovisationCapstoneCastQueue.isEmpty()) {
            gameBroadcastService.broadcastGameState(gameData);
            return;
        }

        UUID cardId = gameData.pendingImprovisationCapstoneCastQueue.removeFirst();
        ExiledCardEntry exiledEntry = gameData.findExiledCard(cardId);
        if (exiledEntry == null) {
            castNextFromQueue(gameData, playerId);
            return;
        }

        Card card = exiledEntry.card();
        gameData.removeFromExile(cardId);

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
                graveyardService.addCardToGraveyard(gameData, playerId, card);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(card.getName() + " has no valid targets."));
                castNextFromQueue(gameData, playerId);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(card, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, firstCandidates,
                    "Choose a target for " + card.getName() + ".");
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " casts " + card.getName() + " without paying its mana cost — choosing target."));
            return;
        }

        gameData.stack.add(new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        ));
        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " casts " + card.getName() + " without paying its mana cost (Improvisation Capstone)."));
        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        castNextFromQueue(gameData, playerId);
    }
}
