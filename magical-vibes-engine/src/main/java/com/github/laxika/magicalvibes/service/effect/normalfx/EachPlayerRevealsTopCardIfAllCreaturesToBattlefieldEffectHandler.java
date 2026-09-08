package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardIfAllCreaturesToBattlefieldEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class EachPlayerRevealsTopCardIfAllCreaturesToBattlefieldEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardIfAllCreaturesToBattlefieldEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        String sourceName = entry.getCard().getName();
        List<RevealedCard> revealedCards = new ArrayList<>();
        boolean allCreatures = true;

        for (UUID playerId : apnapOrder(gameData)) {
            List<Card> deck = gameData.playerDecks.get(playerId);
            String playerName = gameData.playerIdToName.get(playerId);
            if (deck == null || deck.isEmpty()) {
                gameLogService.append(gameData,
                        GameLog.text(playerName + "'s library is empty; no card is revealed (" + sourceName + ")."));
                continue;
            }

            Card topCard = deck.getFirst();
            revealedCards.add(new RevealedCard(playerId, topCard));
            allCreatures &= topCard.hasType(CardType.CREATURE);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " reveals ", topCard, " from the top of their library (" + sourceName + ")."));
        }

        if (!allCreatures || revealedCards.isEmpty()) {
            log.info("Game {} - {} does not put revealed cards onto the battlefield", gameData.id, sourceName);
            return;
        }

        var enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        List<RevealedCard> cardsToEnter = new ArrayList<>();
        for (RevealedCard revealedCard : revealedCards) {
            if (gameQueryService.isCardBlockedFromEnteringFromZone(gameData, revealedCard.card(), Zone.LIBRARY)) {
                gameLogService.append(gameData, GameLog.cardThen(revealedCard.card(),
                        " can't enter the battlefield from a library; it stays on top of the library."));
                continue;
            }

            List<Card> deck = gameData.playerDecks.get(revealedCard.playerId());
            deck.removeFirst();
            cardsToEnter.add(revealedCard);
        }

        List<RevealedPermanent> enteredPermanents = new ArrayList<>();
        for (RevealedCard revealedCard : cardsToEnter) {
            Permanent permanent = new Permanent(revealedCard.card());
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, revealedCard.playerId(), permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            UUID controllerId = findController(gameData, permanent);
            if (controllerId != null) {
                enteredPermanents.add(new RevealedPermanent(controllerId, revealedCard.card()));
                gameLogService.append(gameData,
                        GameLog.entersBattlefieldUnder(revealedCard.card(),
                                gameData.playerIdToName.get(controllerId)));
            }
        }

        for (RevealedPermanent enteredPermanent : enteredPermanents) {
            battlefieldEntryService.handleCreatureEnteredBattlefield(
                    gameData, enteredPermanent.playerId(), enteredPermanent.card(), null, false);
        }
    }

    private UUID findController(GameData gameData, Permanent permanent) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (gameData.playerBattlefields.getOrDefault(playerId, List.of()).contains(permanent)) {
                return playerId;
            }
        }
        return null;
    }

    private List<UUID> apnapOrder(GameData gameData) {
        List<UUID> order = new ArrayList<>();
        order.add(gameData.activePlayerId);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(gameData.activePlayerId)) {
                order.add(playerId);
            }
        }
        return order;
    }

    private record RevealedCard(UUID playerId, Card card) {
    }

    private record RevealedPermanent(UUID playerId, Card card) {
    }
}
