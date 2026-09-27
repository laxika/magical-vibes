package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrizzledHuntmasterEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.OutsideGameCards;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Grizzled Huntmaster's hand search and conjured creature sequence. */
@Component
@RequiredArgsConstructor
public class GrizzledHuntmasterEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final PlayerInputService playerInputService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GrizzledHuntmasterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        GrizzledHuntmasterEffect huntmaster = (GrizzledHuntmasterEffect) effect;
        UUID controllerId = entry.getControllerId();

        if (huntmaster.conjureSourceCard() != null) {
            conjureCopies(gameData, controllerId, huntmaster.conjureSourceCard(), huntmaster.handExiledCount());
            return;
        }

        if (huntmaster.cardName() == null) {
            beginInitialHandChoice(gameData, entry, huntmaster);
            return;
        }

        if (!huntmaster.sameNameCardsExiled()) {
            List<Card> matchingCards = matchingCards(gameData, controllerId, huntmaster.cardName());
            if (!matchingCards.isEmpty()) {
                playerInputService.beginMultiZoneExileChoice(
                        gameData, controllerId, matchingCards, controllerId, huntmaster.cardName(), false,
                        new GrizzledHuntmasterEffect(huntmaster.cardName(), huntmaster.handExiledCount(), true, null));
                return;
            }
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
        }

        beginOutsideGameChoice(gameData, controllerId,
                huntmaster.handExiledCount() + (huntmaster.sameNameCardsExiled() ? entry.getEventValue() : 0),
                huntmaster.cardName());
    }

    private void beginInitialHandChoice(GameData gameData, StackEntry entry, GrizzledHuntmasterEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID originalTargetId = entry.getTargetId();
        entry.setTargetId(controllerId);
        try {
            playerInteractionSupport.resolveHandRevealAndChooseWithChosenCardThen(
                    gameData, entry, 1, List.of(), List.of(CardType.CREATURE), null,
                    false, true, null, true, false, 0, false,
                    false, false, false, 0, null, effect);
        } finally {
            entry.setTargetId(originalTargetId);
        }
    }

    private void beginOutsideGameChoice(GameData gameData, UUID controllerId, int conjureCount, String cardName) {
        List<Card> creatureCards = OutsideGameCards.view(gameData, controllerId).stream()
                .filter(card -> card.hasType(CardType.CREATURE))
                .toList();
        if (creatureCards.isEmpty()) {
            gameLogService.append(gameData, GameLog.text(
                    gameData.playerIdToName.get(controllerId)
                            + " has no creature card outside the game to choose for Grizzled Huntmaster."));
            return;
        }

        playerInputService.beginOutsideGameCardChoice(
                gameData, controllerId, creatureCards, new CardTypePredicate(CardType.CREATURE),
                "creature card", true, true,
                new GrizzledHuntmasterEffect(cardName, conjureCount, true, null));
    }

    private List<Card> matchingCards(GameData gameData, UUID controllerId, String cardName) {
        List<Card> matchingCards = new ArrayList<>();
        matchingCards.addAll(gameData.playerHands.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.getName().equals(cardName)).toList());
        matchingCards.addAll(gameData.playerDecks.getOrDefault(controllerId, List.of()).stream()
                .filter(card -> card.getName().equals(cardName)).toList());
        return matchingCards;
    }

    private void conjureCopies(GameData gameData, UUID controllerId, Card sourceCard, int count) {
        for (int i = 0; i < count; i++) {
            Card copy = sourceCard.createCardCopy();
            copy.setOwnerId(controllerId);
            copy.freeze();
            gameData.addCardToHand(controllerId, copy);
            gameLogService.append(gameData, GameLog.cardThen(copy, " is conjured into "
                    + gameData.playerIdToName.get(controllerId) + "'s hand."));
        }
    }
}
