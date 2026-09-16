package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Time Sidewalk's opening-hand ability. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffectHandler
        implements NormalEffectHandlerBean {

    private static final int TIME_WALK_TOKEN_CARD_COUNT = 4;

    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromOpeningHandAndCreateTimeWalkTokenCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        UUID controllerId = entry.getControllerId();
        if (sourceCard == null || controllerId == null) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (hand == null || library == null) {
            return;
        }

        Card cardInHand = hand.stream()
                .filter(card -> sourceCard.getId().equals(card.getId()))
                .findFirst()
                .orElse(null);
        if (cardInHand == null) {
            return;
        }

        hand.remove(cardInHand);
        exileService.exileCard(gameData, controllerId, cardInHand);
        for (int i = 0; i < TIME_WALK_TOKEN_CARD_COUNT; i++) {
            library.add(createTimeWalkTokenCard(controllerId));
        }
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);

        gameLogService.append(gameData, GameLog.cardThen(cardInHand,
                " is exiled from the opening hand; four Time Walk token cards are shuffled into the library."));
        log.info("Game {} - {} exiles {} from the opening hand and shuffles four Time Walk token cards into the library",
                gameData.id, gameData.playerIdToName.get(controllerId), cardInHand.getName());
    }

    private Card createTimeWalkTokenCard(UUID ownerId) {
        Card tokenCard = new Card();
        tokenCard.setName("Time Walk");
        tokenCard.setType(CardType.SORCERY);
        tokenCard.setManaCost("{1}{U}");
        tokenCard.setColor(CardColor.BLUE);
        tokenCard.setColors(List.of(CardColor.BLUE));
        tokenCard.setCardText("Take an extra turn after this one.");
        tokenCard.setOwnerId(ownerId);
        tokenCard.setToken(true);
        tokenCard.setTokenCard(true);
        tokenCard.addEffect(EffectSlot.SPELL, new ControllerExtraTurnEffect(1));
        tokenCard.freeze();
        return tokenCard;
    }
}
