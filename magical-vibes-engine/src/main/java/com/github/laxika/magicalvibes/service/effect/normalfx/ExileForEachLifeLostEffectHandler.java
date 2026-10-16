package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileForEachLifeLostEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Resolves "for each 1 life you lost, exile a permanent you control or a card from your hand or
 * graveyard" (Lich's Mastery). The amount is the life lost by the triggering event. The controller
 * picks exactly that many objects across the three zones, or all of them when fewer are available.
 */
@Component
@RequiredArgsConstructor
public class ExileForEachLifeLostEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final ExileService exileService;
    private final PermanentRemovalService permanentRemovalService;
    @Autowired
    @Lazy
    private GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileForEachLifeLostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        int count = entry.getEventValue();
        List<UUID> permanentIds = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            permanentIds.add(permanent.getId());
        }
        List<UUID> cardIds = new ArrayList<>();
        gameData.playerHands.getOrDefault(controllerId, List.of()).forEach(card -> cardIds.add(card.getId()));
        gameData.playerGraveyards.getOrDefault(controllerId, List.of()).forEach(card -> cardIds.add(card.getId()));
        int available = permanentIds.size() + cardIds.size();
        if (count <= 0 || available == 0) {
            return;
        }
        if (available <= count) {
            exile(gameData, controllerId, permanentIds, cardIds);
            return;
        }
        playerInputService.beginMultiPermanentChoice(gameData, controllerId, permanentIds, cardIds, count,
                new MultiPermanentChoiceContext.ExileForEachLifeLost(),
                "Exile " + count + " permanent" + (count != 1 ? "s" : "") + " you control or card"
                        + (count != 1 ? "s" : "") + " from your hand or graveyard.");
    }

    public void completeChoice(GameData gameData, UUID controllerId, List<UUID> chosenIds) {
        List<UUID> permanentIds = new ArrayList<>();
        List<UUID> cardIds = new ArrayList<>();
        for (UUID id : chosenIds) {
            boolean onBattlefield = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                    .anyMatch(permanent -> permanent.getId().equals(id));
            (onBattlefield ? permanentIds : cardIds).add(id);
        }
        exile(gameData, controllerId, permanentIds, cardIds);
    }

    private void exile(GameData gameData, UUID controllerId, List<UUID> permanentIds, List<UUID> cardIds) {
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : cardIds) {
                if (graveyard != null) {
                    Card card = graveyard.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                    if (card != null) {
                        graveyard.remove(card);
                        graveyardService.notifyCardsExiledFromGraveyard(gameData, controllerId, card);
                        exileService.exileCard(gameData, controllerId, card);
                        gameLogService.append(gameData, GameLog.textCardText(
                                playerName + " exiles ", card, " from their graveyard."));
                        continue;
                    }
                }
                List<Card> hand = gameData.playerHands.get(controllerId);
                Card card = hand == null ? null
                        : hand.stream().filter(c -> c.getId().equals(cardId)).findFirst().orElse(null);
                if (card != null) {
                    hand.remove(card);
                    exileService.exileCard(gameData, controllerId, card);
                    gameLogService.append(gameData, GameLog.textCardText(
                            playerName + " exiles ", card, " from their hand."));
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                    .filter(candidate -> candidate.getId().equals(permanentId)).findFirst().orElse(null);
            if (permanent == null) {
                continue;
            }
            Card exiledCard = permanent.getCard();
            if (permanentRemovalService.removePermanentToExile(gameData, permanent)) {
                gameLogService.append(gameData, GameLog.textCardText(
                        playerName + " exiles ", exiledCard, " from the battlefield."));
            }
        }
        permanentRemovalService.removeOrphanedAuras(gameData);
    }
}
