package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilCreatureToBattlefieldOrHandByManaValueEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.LegendRuleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RevealUntilCreatureToBattlefieldOrHandByManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final LegendRuleService legendRuleService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealUntilCreatureToBattlefieldOrHandByManaValueEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);
        List<Card> deck = gameData.playerDecks.get(controllerId);
        if (deck == null || deck.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s library is empty — no cards are revealed."));
            return;
        }

        int landCount = gameData.playerBattlefields
                .getOrDefault(controllerId, List.of())
                .stream()
                .filter(permanent -> gameQueryService.isLand(gameData, permanent))
                .mapToInt(ignored -> 1)
                .sum();

        List<Card> revealedCards = new ArrayList<>();
        Card creatureCard = null;
        while (!deck.isEmpty()) {
            Card card = deck.removeFirst();
            revealedCards.add(card);
            if (card.hasType(CardType.CREATURE)) {
                creatureCard = card;
                break;
            }
        }

        gameLogService.append(gameData, GameLog.text(
                playerName + " reveals "
                        + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                        + " from the top of their library."));

        Permanent enteredPermanent = null;
        if (creatureCard == null) {
            gameLogService.append(gameData, GameLog.text(
                    playerName + " reveals their entire library without finding a creature card."));
        } else if (creatureCard.getManaValue() <= landCount) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, creatureCard, Zone.LIBRARY)) {
                gameLogService.append(gameData, GameLog.cardThen(creatureCard,
                        " can't enter the battlefield from a library; it stays in the library."));
            } else {
                revealedCards.remove(creatureCard);
                enteredPermanent = new Permanent(creatureCard, Zone.LIBRARY);
                battlefieldEntryService.putPermanentOntoBattlefield(
                        gameData, controllerId, enteredPermanent);
                gameLogService.append(gameData,
                        GameLog.entersBattlefieldUnder(creatureCard, playerName));
                battlefieldEntryService.handleCreatureEnteredBattlefield(
                        gameData, controllerId, creatureCard, null, false);
            }
        } else {
            revealedCards.remove(creatureCard);
            gameData.addCardToHand(controllerId, creatureCard);
            gameLogService.append(gameData, GameLog.text(
                    playerName + " puts " + creatureCard.getName() + " into their hand."));
        }

        Collections.shuffle(revealedCards);
        deck.addAll(revealedCards);

        if (enteredPermanent != null && !gameData.interaction.isAwaitingInput()) {
            legendRuleService.checkLegendRule(gameData, controllerId);
        }
    }
}
