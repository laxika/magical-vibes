package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Gutmorn's discard trigger and preserves the discarded card's characteristics. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfDiscardedCardIntoChosenPlayerHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardingPlayerId = entry.getTargetId();
        Card discardedCard = entry.getDiscardedCardSnapshot();
        if (discardingPlayerId == null || discardedCard == null
                || !gameData.playerIds.contains(discardingPlayerId)) {
            return;
        }

        List<UUID> otherPlayerIds = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(discardingPlayerId))
                .toList();
        if (otherPlayerIds.isEmpty()) {
            return;
        }
        if (otherPlayerIds.size() == 1) {
            conjure(gameData, otherPlayerIds.getFirst(), discardedCard);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.GutmornDiscardedCardPlayerChoice(
                        entry.getCard(), entry.getControllerId(), entry.getSourcePermanentId(),
                        discardingPlayerId, discardedCard));
        playerInputService.beginPlayerChoice(gameData, discardingPlayerId, otherPlayerIds,
                entry.getCard().getName() + " - choose another player.");
    }

    public void completeChoice(GameData gameData, UUID chosenPlayerId,
                               PermanentChoiceContext.GutmornDiscardedCardPlayerChoice context) {
        if (!gameData.playerIds.contains(chosenPlayerId)
                || chosenPlayerId.equals(context.discardingPlayerId())) {
            return;
        }
        conjure(gameData, chosenPlayerId, context.discardedCard());
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private void conjure(GameData gameData, UUID recipientId, Card discardedCard) {
        Card copy = discardedCard.createCardCopy();
        copy.setOwnerId(recipientId);
        copy.freeze();
        gameData.perpetualAnyColorManaForCastCardIds.add(copy.getId());
        gameData.addCardToHand(recipientId, copy);
        gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                + gameData.playerIdToName.get(recipientId) + "'s hand."));
    }
}
