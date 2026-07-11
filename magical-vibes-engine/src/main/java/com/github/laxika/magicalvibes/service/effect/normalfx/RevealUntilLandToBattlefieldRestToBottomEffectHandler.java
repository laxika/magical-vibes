package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandToBattlefieldRestToBottomEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilLandToBattlefieldRestToBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final LibraryRevealSupport libraryRevealSupport;
    private final BattlefieldEntryService battlefieldEntryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilLandToBattlefieldRestToBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        String cardName = entry.getCard().getName();
        List<Card> deck = gameData.playerDecks.get(controllerId);

        // Reveal from the top until we reveal a land card (or the library runs out).
        List<Card> revealed = new ArrayList<>();
        Card land = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealed.add(card);
            if (card.hasType(CardType.LAND)) {
                land = card;
                break;
            }
        }

        if (revealed.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + "'s library is empty — no cards are revealed with " + cardName + ".");
            return;
        }

        String revealedNames = revealed.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData,
                playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + ".");

        // The land (if any) enters the battlefield; the rest go on the bottom in any order.
        List<Card> rest = new ArrayList<>(revealed);
        if (land != null) {
            rest.remove(land);
            Permanent perm = new Permanent(land);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, controllerId, perm);
            gameBroadcastService.logAndBroadcast(gameData,
                    land.getName() + " enters the battlefield under " + playerName + "'s control.");
        } else {
            gameBroadcastService.logAndBroadcast(gameData,
                    playerName + " reveals their entire library — no land found.");
        }

        log.info("Game {} - {} resolving {} — land={}, {} cards to bottom",
                gameData.id, playerName, cardName, land != null ? land.getName() : "none", rest.size());

        if (!rest.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, rest);
        }
    }
}
