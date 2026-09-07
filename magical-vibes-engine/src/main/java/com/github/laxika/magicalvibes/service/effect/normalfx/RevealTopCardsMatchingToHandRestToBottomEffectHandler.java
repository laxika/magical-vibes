package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsMatchingToHandRestToBottomEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RevealTopCardsMatchingToHandRestToBottomEffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LibraryRevealSupport libraryRevealSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RevealTopCardsMatchingToHandRestToBottomEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RevealTopCardsMatchingToHandRestToBottomEffect e =
                (RevealTopCardsMatchingToHandRestToBottomEffect) effect;

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()) : null;
        CardSubtype chosenSubtype = source != null ? source.getChosenSubtype() : null;

        LibraryRevealSupport.TopCardsResult result =
                libraryRevealSupport.takeTopCardsFromLibrary(gameData, entry, e.count());
        if (result == null) return;
        UUID controllerId = result.controllerId();
        List<Card> topCards = result.topCards();
        String playerName = result.playerName();
        String cardName = entry.getCard().getName();

        String revealedNames = topCards.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
        gameLogService.append(gameData, GameLog.text(playerName + " reveals " + revealedNames + " from the top of their library with " + cardName + "."));

        List<Card> toHand = new ArrayList<>();
        List<Card> toBottom = new ArrayList<>();
        for (Card card : topCards) {
            if (matches(gameData, card, e, chosenSubtype, controllerId)) {
                toHand.add(card);
            } else {
                toBottom.add(card);
            }
        }

        for (Card card : toHand) {
            gameData.addCardToHand(controllerId, card);
        }
        if (!toHand.isEmpty()) {
            String handNames = toHand.stream().map(Card::getName).reduce((a, b) -> a + ", " + b).orElse("");
            gameLogService.append(gameData, GameLog.text(playerName + " puts " + handNames + " into their hand."));
        }

        log.info("Game {} - {} resolving {} — {} to hand, {} to bottom",
                gameData.id, playerName, cardName, toHand.size(), toBottom.size());

        if (!toBottom.isEmpty()) {
            libraryRevealSupport.reorderRemainingToBottom(gameData, controllerId, toBottom);
        }
    }

    private boolean matches(GameData gameData, Card card,
                            RevealTopCardsMatchingToHandRestToBottomEffect e,
                            CardSubtype chosenSubtype, UUID controllerId) {
        if (e.matcher() != null) {
            return predicateEvaluationService.matchesCardPredicate(
                    card, e.matcher(), null, gameData, controllerId);
        }
        return chosenSubtype != null && isCreatureOfChosenType(card, chosenSubtype);
    }

    private boolean isCreatureOfChosenType(Card card, CardSubtype chosenSubtype) {
        boolean creature = LibraryRevealSupport.matchesCardTypes(card, Set.of(CardType.CREATURE));
        if (!creature) return false;
        return card.hasKeyword(Keyword.CHANGELING)
                || card.getSubtypes().contains(chosenSubtype);
    }
}
