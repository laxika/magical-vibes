package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ImprovisationCapstoneCastSupport {

    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GraveyardService graveyardService;
    private final PlayerInputService playerInputService;
    private final TriggerCollectionService triggerCollectionService;

    public void castChosenSpellsWithoutPaying(GameData gameData, Player player, List<UUID> cardIds) {
        gameData.interaction.clearAwaitingInput();

        if (cardIds == null || cardIds.isEmpty()) {
            String logEntry = player.getUsername() + " casts no spells from Improvisation Capstone.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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

        StackEntryType spellType = mapCardTypeToSpellType(card);
        List<CardEffect> spellEffects = new ArrayList<>(card.getEffects(EffectSlot.SPELL));
        String playerName = gameData.playerIdToName.get(playerId);

        if (EffectResolution.needsTarget(card)) {
            List<UUID> validTargets = buildValidSpellTargets(gameData, card);
            if (validTargets.isEmpty()) {
                graveyardService.addCardToGraveyard(gameData, playerId, card);
                gameBroadcastService.logAndBroadcast(gameData, card.getName() + " has no valid targets.");
                castNextFromQueue(gameData, playerId);
                return;
            }

            gameData.interaction.setPermanentChoiceContext(
                    new PermanentChoiceContext.ExileCastSpellTarget(card, playerId, spellEffects, spellType));
            playerInputService.beginPermanentChoice(gameData, playerId, validTargets,
                    "Choose a target for " + card.getName() + ".");
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " casts " + card.getName() + " without paying its mana cost — choosing target.");
            return;
        }

        gameData.stack.add(new StackEntry(
                spellType, card, playerId, card.getName(),
                spellEffects, 0, (UUID) null, null
        ));
        gameData.recordSpellCast(playerId, card);
        gameData.priorityPassedBy.clear();
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " casts " + card.getName() + " without paying its mana cost (Improvisation Capstone).");
        triggerCollectionService.checkSpellCastTriggers(gameData, card, playerId, false);
        castNextFromQueue(gameData, playerId);
    }

    private List<UUID> buildValidSpellTargets(GameData gameData, Card card) {
        Set<TargetType> allowedTargets = EffectResolution.computeAllowedTargets(card);
        List<UUID> validTargets = new ArrayList<>();

        if (allowedTargets.contains(TargetType.PERMANENT)) {
            for (UUID pid : gameData.orderedPlayerIds) {
                List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
                if (battlefield == null) {
                    continue;
                }
                for (Permanent p : battlefield) {
                    if (card.getTargetFilter() instanceof PermanentPredicateTargetFilter filter) {
                        if (predicateEvaluationService.matchesPermanentPredicate(gameData, p, filter.predicate())) {
                            validTargets.add(p.getId());
                        }
                    } else if (gameQueryService.isCreature(gameData, p)) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        if (allowedTargets.contains(TargetType.PLAYER)) {
            validTargets.addAll(gameData.orderedPlayerIds);
        }

        return validTargets;
    }

    private static StackEntryType mapCardTypeToSpellType(Card card) {
        return switch (card.getType()) {
            case INSTANT -> StackEntryType.INSTANT_SPELL;
            case SORCERY -> StackEntryType.SORCERY_SPELL;
            case CREATURE -> StackEntryType.CREATURE_SPELL;
            case ARTIFACT -> StackEntryType.ARTIFACT_SPELL;
            case ENCHANTMENT -> StackEntryType.ENCHANTMENT_SPELL;
            case PLANESWALKER -> StackEntryType.PLANESWALKER_SPELL;
            default -> StackEntryType.SORCERY_SPELL;
        };
    }
}
