package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandThenMayPayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceCardFromGraveyardToOwnerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnEnchantedCreatureToOwnerHandThenMayPayEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureToOwnerHandThenMayPayEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnEnchantedCreatureToOwnerHandThenMayPayEffect) effect;

        if (gameData.resolvedMayAccepted != null) {
            boolean accepted = gameData.resolvedMayAccepted;
            gameData.resolvedMayAccepted = null;
            if (accepted) {
                returnSourceCardToOwnerHand(gameData, entry);
            }
            return;
        }

        UUID dyingCreatureCardId = e.dyingCreatureCardId();
        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card ID)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        if (creatureCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (creature not in graveyard)."));
            log.info("Game {} - {} death trigger fizzles (creature card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCreatureCardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCreatureCardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCreatureCardId);
        gameData.addCardToHand(ownerId, creatureCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        gameLogService.append(gameData,
                GameLog.builder().card(creatureCard)
                        .text(" returns from graveyard to " + ownerName + "'s hand.").build());
        log.info("Game {} - {} returns {} from graveyard to {}'s hand",
                gameData.id, entry.getCard().getName(), creatureCard.getName(), ownerName);

        gameData.resolvingMayEffectFromStack = true;
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getCard(),
                entry.getControllerId(),
                List.of(new ReturnSourceCardFromGraveyardToOwnerHandEffect()),
                entry.getCard().getName() + " - Pay " + e.manaCost() + " to return this card to your hand?",
                null,
                e.manaCost()
        ));
    }

    private void returnSourceCardToOwnerHand(GameData gameData, StackEntry entry) {
        UUID sourceCardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, sourceCardId);
        if (sourceCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} return-to-hand follow-up fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), sourceCardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, sourceCardId);
        gameData.addCardToHand(ownerId, sourceCard);
        gameLogService.append(gameData,
                GameLog.builder().card(sourceCard)
                        .text(" returns from graveyard to its owner's hand.").build());
        log.info("Game {} - {} returns from graveyard to its owner's hand",
                gameData.id, entry.getCard().getName());
    }
}
