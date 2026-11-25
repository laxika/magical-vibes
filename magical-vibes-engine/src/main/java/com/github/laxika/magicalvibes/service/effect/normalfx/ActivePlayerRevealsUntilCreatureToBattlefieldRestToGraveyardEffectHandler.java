package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.BattlefieldEntryCard;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryBatchSupport;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The active player reveals cards from the top of their library until a creature card, puts that card onto the
 * battlefield and the other revealed cards into their graveyard (Oath of Druids). The creature enters through the
 * entry batch, so its as-enters choices and replacements apply, and one that can't enter stays in the library
 * (CR 614.1c, 614.12).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryBatchSupport battlefieldEntryBatchSupport;
    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID playerId = entry.getActivePlayerId() != null
                ? entry.getActivePlayerId() : entry.getControllerId();
        String playerName = gameData.playerIdToName.get(playerId);
        List<Card> deck = gameData.playerDecks.get(playerId);

        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        List<Card> revealedCards = new ArrayList<>();
        Card foundCreature = null;
        for (Card card : deck) {
            revealedCards.add(card);
            if (card.hasType(CardType.CREATURE)) {
                foundCreature = card;
                break;
            }
        }

        String revealedNames = revealedCards.stream()
                .map(Card::getName)
                .collect(Collectors.joining(", "));
        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals " + revealedNames + " from the top of their library."));
        if (foundCreature == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no creature card was found."));
        } else {
            revealedCards.remove(foundCreature);
        }

        for (Card card : revealedCards) {
            deck.removeIf(c -> c.getId().equals(card.getId()));
            graveyardService.addCardToGraveyard(gameData, playerId, card, Zone.LIBRARY);
        }
        if (foundCreature != null) {
            battlefieldEntryBatchSupport.begin(gameData, List.of(
                    new BattlefieldEntryCard(playerId, playerId, foundCreature, Zone.LIBRARY, null)));
        }

        log.info("Game {} - {} reveals {} cards, creature found={}",
                gameData.id, playerName, revealedCards.size() + (foundCreature != null ? 1 : 0),
                foundCreature != null ? foundCreature.getName() : "none");
    }
}
