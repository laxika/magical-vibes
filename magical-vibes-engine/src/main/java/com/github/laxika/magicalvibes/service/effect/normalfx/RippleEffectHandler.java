package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RippleEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Ripple's reveal and same-name free-cast choices. */
@Component
@RequiredArgsConstructor
public class RippleEffectHandler implements NormalEffectHandlerBean {

    private final RippleFreeCastSupport rippleFreeCastSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RippleEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RippleEffect ripple = (RippleEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<Card> deck = gameData.playerDecks.get(controllerId);
        int revealCount = Math.min(Math.max(ripple.count(), 0), deck.size());
        if (revealCount == 0) {
            return;
        }

        List<Card> revealed = LibraryRevealSupport.takeTopCards(deck, revealCount);
        String cardName = resolveRippleCardName(gameData, entry);
        GameLog.Builder revealLog = GameLog.builder()
                .text(gameData.playerIdToName.get(controllerId) + " reveals ");
        for (int i = 0; i < revealed.size(); i++) {
            if (i > 0) {
                revealLog.text(", ");
            }
            revealLog.card(revealed.get(i));
        }
        revealLog.text(".");
        gameLogService.append(gameData, revealLog.build());

        rippleFreeCastSupport.offerOrBottom(
                gameData,
                controllerId,
                controllerId,
                cardName,
                revealed);
    }

    private String resolveRippleCardName(GameData gameData, StackEntry entry) {
        Card triggeringCard = findCardById(gameData, entry.getTriggeringCardId());
        return triggeringCard != null ? triggeringCard.getName() : entry.getCard().getName();
    }

    private static Card findCardById(GameData gameData, UUID cardId) {
        if (cardId == null) {
            return null;
        }
        for (List<Card> cards : gameData.playerHands.values()) {
            Card found = cards.stream().filter(card -> cardId.equals(card.getId())).findFirst().orElse(null);
            if (found != null) {
                return found;
            }
        }
        for (List<Card> cards : gameData.playerDecks.values()) {
            Card found = cards.stream().filter(card -> cardId.equals(card.getId())).findFirst().orElse(null);
            if (found != null) {
                return found;
            }
        }
        for (List<Card> cards : gameData.playerGraveyards.values()) {
            Card found = cards.stream().filter(card -> cardId.equals(card.getId())).findFirst().orElse(null);
            if (found != null) {
                return found;
            }
        }
        for (List<Card> cards : gameData.playerCommandZones.values()) {
            Card found = cards.stream().filter(card -> cardId.equals(card.getId())).findFirst().orElse(null);
            if (found != null) {
                return found;
            }
        }
        for (var battlefield : gameData.playerBattlefields.values()) {
            for (var permanent : battlefield) {
                if (cardId.equals(permanent.getCard().getId())) {
                    return permanent.getCard();
                }
            }
        }
        for (var exiled : gameData.exiledCards) {
            if (cardId.equals(exiled.card().getId())) {
                return exiled.card();
            }
        }
        for (StackEntry stackEntry : gameData.stack) {
            if (cardId.equals(stackEntry.getCard().getId())) {
                return stackEntry.getCard();
            }
        }
        return null;
    }
}
