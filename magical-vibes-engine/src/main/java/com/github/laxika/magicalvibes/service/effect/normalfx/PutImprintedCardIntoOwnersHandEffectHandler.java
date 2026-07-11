package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCardIntoOwnersHandEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutImprintedCardIntoOwnersHandEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutImprintedCardIntoOwnersHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        Card imprintedCard = gameData.getImprintedCard(entry.getCard());
        String cardName = entry.getCard().getName();

        if (imprintedCard == null) {
            String logMsg = cardName + "'s ability resolves but no card was exiled.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        // Find the owner by checking which player's exile zone has the card
        ExiledCardEntry exileEntry = gameData.findExiledCard(imprintedCard.getId());

        if (exileEntry == null) {
            String logMsg = cardName + "'s ability resolves but the exiled card is no longer in exile.";
            gameBroadcastService.logAndBroadcast(gameData, logMsg);
            return;
        }

        UUID ownerId = exileEntry.ownerId();

        // Remove from exile zone
        gameData.removeFromExile(imprintedCard.getId());

        // Put into owner's hand
        gameData.addCardToHand(ownerId, imprintedCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        String logMsg = imprintedCard.getName() + " is returned to " + ownerName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);

        log.info("Game {} - {} puts imprinted card {} into {}'s hand",
                gameData.id, cardName, imprintedCard.getName(), ownerName);
    }
}
