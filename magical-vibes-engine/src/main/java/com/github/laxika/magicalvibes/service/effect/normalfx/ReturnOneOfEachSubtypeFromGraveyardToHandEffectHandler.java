package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOneOfEachSubtypeFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.effect.normalfx.GraveyardReturnSupport;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ReturnOneOfEachSubtypeFromGraveyardToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOneOfEachSubtypeFromGraveyardToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnOneOfEachSubtypeFromGraveyardToHandEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);

        for (CardSubtype subtype : e.subtypes()) {
            CardSubtypePredicate filter = new CardSubtypePredicate(subtype);

            if (graveyard == null || graveyard.isEmpty()) {
                break;
            }

            List<Card> matching = graveyard.stream()
                    .filter(card -> predicateEvaluationService.matchesCardPredicate(card, filter, null))
                    .toList();

            if (matching.isEmpty()) {
                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " has no " + subtype.getDisplayName() + " cards in their graveyard.");
                continue;
            }

            if (matching.size() == 1) {
                // Only one match — return it automatically
                Card card = matching.getFirst();
                graveyard.remove(card);
                graveyardService.notifyCardsLeftGraveyard(gameData, controllerId);
                gameData.addCardToHand(controllerId, card);

                String playerName = gameData.playerIdToName.get(controllerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + " returns " + card.getName() + " from graveyard to hand.");
            } else {
                // Multiple matches — queue a choice prompt
                gameData.pendingGraveyardReturnQueue.add(
                        new PendingGraveyardReturnChoice(controllerId, 1, filter,
                                GraveyardChoiceDestination.HAND, false));
            }
        }

        if (!gameData.pendingGraveyardReturnQueue.isEmpty()) {
            graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
        }
    }
}
