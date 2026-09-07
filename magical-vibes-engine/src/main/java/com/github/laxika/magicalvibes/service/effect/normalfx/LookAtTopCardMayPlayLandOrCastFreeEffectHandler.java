package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPlayLandOrCastFreeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/** Resolves Ziatora's Envoy's combat-damage top-card permission. */
@Slf4j
@Component
@RequiredArgsConstructor
public class LookAtTopCardMayPlayLandOrCastFreeEffectHandler implements NormalEffectHandlerBean {

    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LookAtTopCardMayPlayLandOrCastFreeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        LookAtTopCardMayPlayLandOrCastFreeEffect lookEffect =
                (LookAtTopCardMayPlayLandOrCastFreeEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        String playerName = gameData.playerIdToName.get(controllerId);
        String sourceName = entry.getCard().getName();

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty (" + sourceName + ")."));
            return;
        }

        gameLogService.append(gameData,
                GameLog.text(playerName + " looks at the top card of their library ("
                        + sourceName + ")."));

        Card topCard = deck.getFirst();
        if (topCard.hasType(CardType.LAND)) {
            int landsPlayed = gameData.landsPlayedThisTurn.getOrDefault(controllerId, 0);
            if (!controllerId.equals(gameData.activePlayerId)
                    || landsPlayed >= gameData.getMaxLandsThisTurn(controllerId)) {
                putTopCardIntoHand(gameData, controllerId, deck, topCard, playerName);
                return;
            }
        } else {
            int maxManaValue = amountEvaluationService.evaluate(gameData, lookEffect.maxManaValue(),
                    AmountContext.forStackEntry(entry, null));
            if (topCard.isCastOnlyFromGraveyard() || topCard.getManaValue() > maxManaValue) {
                putTopCardIntoHand(gameData, controllerId, deck, topCard, playerName);
                return;
            }
        }

        String action = topCard.hasType(CardType.LAND) ? "Play " : "Cast ";
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                topCard,
                controllerId,
                List.of(lookEffect),
                sourceName + " — " + action + topCard.getName() + " from the top of your library?"
        ));
        log.info("Game {} - {} offers {} for a top-card play or free cast",
                gameData.id, playerName, topCard.getName());
    }

    private void putTopCardIntoHand(GameData gameData, UUID controllerId, List<Card> deck,
                                    Card topCard, String playerName) {
        deck.removeFirst();
        gameData.playerHands.get(controllerId).add(topCard);
        gameLogService.append(gameData,
                GameLog.text(playerName + " puts the top card of their library into their hand."));
        log.info("Game {} - {} puts the top card into hand ({})",
                gameData.id, playerName, topCard.getName());
    }
}
