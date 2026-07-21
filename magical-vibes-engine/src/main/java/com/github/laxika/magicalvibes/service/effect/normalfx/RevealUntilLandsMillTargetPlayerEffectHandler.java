package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealUntilLandsMillTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilLandsMillTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null) {
            return;
        }

        int landCount = ((RevealUntilLandsMillTargetPlayerEffect) effect).landCount();
        String targetName = gameData.playerIdToName.get(targetPlayerId);

        // Reveal cards from the top of the target player's library until landCount lands are revealed
        // (or the library empties).
        List<Card> deck = gameData.playerDecks.get(targetPlayerId);
        List<Card> revealedCards = new ArrayList<>();
        int landsRevealed = 0;

        while (deck != null && !deck.isEmpty() && landsRevealed < landCount) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.LAND)) {
                landsRevealed++;
            }
        }

        if (revealedCards.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + "'s library is empty — no cards are revealed."));
            return;
        }

        String revealedNames = revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "));
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(targetName + " reveals " + revealedNames + "."));

        // All revealed cards go to the target player's graveyard.
        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, targetPlayerId, card);
        }

        log.info("Game {} - {} reveals {} cards ({} lands) to their graveyard from Mind Funeral-style mill",
                gameData.id, targetName, revealedCards.size(), landsRevealed);
    }
}
