package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutSourceCardFromGraveyardIntoLibraryNFromTopEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PutSourceCardFromGraveyardIntoLibraryNFromTopEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutSourceCardFromGraveyardIntoLibraryNFromTopEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int position = ((PutSourceCardFromGraveyardIntoLibraryNFromTopEffect) effect).position();

        UUID cardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} tuck-on-death trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, cardId);
        List<Card> library = gameData.playerDecks.get(ownerId);
        // If the library has fewer cards than the position, the card goes on the bottom (CR 701).
        library.add(Math.min(position, library.size()), sourceCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        String ordinal = switch (position) {
            case 0 -> "on top of";
            case 1 -> "second from the top of";
            case 2 -> "third from the top of";
            default -> (position + 1) + "th from the top of";
        };
        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder().card(sourceCard).text(" is put " + ordinal + " " + ownerName + "'s library.").build());
        log.info("Game {} - {} put {} {}'s library from graveyard (position {})", gameData.id, sourceCard.getName(), ordinal, ownerName, position);
    }
}
