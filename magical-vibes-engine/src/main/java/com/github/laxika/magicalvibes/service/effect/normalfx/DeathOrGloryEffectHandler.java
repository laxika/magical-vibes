package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardPileDisposition;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DeathOrGloryEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DeathOrGloryEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DeathOrGloryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        List<Card> creatureCards = graveyard == null
                ? List.of()
                : graveyard.stream().filter(card -> card.hasType(CardType.CREATURE)).toList();

        if (creatureCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + "'s graveyard has no creature cards for ",
                    entry.getCard(), "."));
            return;
        }

        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElse(null);
        if (opponentId == null) {
            return;
        }

        Map<UUID, UUID> cardOwners = new HashMap<>();
        creatureCards.forEach(card -> cardOwners.put(card.getId(), controllerId));
        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), creatureCards, cardOwners, List.of(), List.of(),
                CardPileDisposition.OPPONENT_CHOOSES_EXILE, false));

        playerInputService.beginMultiGraveyardChoice(gameData, controllerId, creatureCards, creatureCards.size(),
                "Separate the creature cards in your graveyard into two piles. Select cards for Pile 1 "
                        + "(unselected form Pile 2).");
    }
}
