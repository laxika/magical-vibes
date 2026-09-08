package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.BattlefieldEntryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffectHandler
        implements NormalEffectHandlerBean {

    private final BattlefieldEntryService battlefieldEntryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var typedEffect = (EachPlayerRevealsTopCardsLandsToBattlefieldTappedRestExiledEffect) effect;
        if (typedEffect.count() <= 0) {
            return;
        }

        Set<CardType> enterTappedTypes = battlefieldEntryService.snapshotEnterTappedTypes(gameData);
        for (UUID playerId : apnapOrder(gameData)) {
            revealForPlayer(gameData, playerId, typedEffect.count(), enterTappedTypes, entry.getCard().getName());
        }
    }

    private void revealForPlayer(GameData gameData, UUID playerId, int count, Set<CardType> enterTappedTypes,
                                 String sourceName) {
        List<Card> library = gameData.playerDecks.get(playerId);
        String playerName = gameData.playerIdToName.get(playerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        int revealCount = Math.min(count, library.size());
        List<Card> revealedCards = new ArrayList<>(library.subList(0, revealCount));
        library.subList(0, revealCount).clear();
        gameLogService.append(gameData, GameLog.text(playerName + " reveals "
                + revealedCards.stream().map(Card::getName).collect(Collectors.joining(", "))
                + " from the top of their library (" + sourceName + ")."));

        List<Card> landCards = new ArrayList<>();
        for (Card card : revealedCards) {
            if (card.hasType(CardType.LAND)) {
                landCards.add(card);
            } else {
                gameData.addToExile(playerId, card);
            }
        }

        List<Permanent> simultaneouslyEntered = new ArrayList<>();
        for (Card landCard : landCards) {
            Permanent permanent = new Permanent(landCard);
            permanent.tap();
            battlefieldEntryService.putPermanentOntoBattlefield(
                    gameData, playerId, permanent, enterTappedTypes, simultaneouslyEntered);
            simultaneouslyEntered.add(permanent);
            gameLogService.append(gameData, GameLog.entersBattlefieldTappedUnder(landCard, playerName));
        }
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
}
