package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
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
public class ActivePlayerRevealsUntilCreatureToBattlefieldRestToGraveyardEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GraveyardService graveyardService;
    private final LegendRuleService legendRuleService;

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
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
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

        Permanent permanent = null;
        if (foundCreature != null) {
            revealedCards.remove(foundCreature);
            permanent = new Permanent(foundCreature);
            battlefieldEntryService.putPermanentOntoBattlefield(gameData, playerId, permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldUnder(foundCreature, playerName));
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, playerId, foundCreature, null, false);
        } else {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library — no creature card was found."));
        }

        for (Card card : revealedCards) {
            graveyardService.addCardToGraveyard(gameData, playerId, card);
        }

        if (permanent != null && !gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, playerId);
        }

        log.info("Game {} - {} reveals {} cards, creature found={}",
                gameData.id, playerName, revealedCards.size() + (foundCreature != null ? 1 : 0),
                foundCreature != null ? foundCreature.getName() : "none");
    }
}
