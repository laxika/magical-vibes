package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSameNameCardsFromHandAndLibraryThenSeekEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Unexpected Conversion's hand/library same-name exile and seek rider. */
@Component
@RequiredArgsConstructor
public class ExileSameNameCardsFromHandAndLibraryThenSeekEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSameNameCardsFromHandAndLibraryThenSeekEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileSameNameCardsFromHandAndLibraryThenSeekEffect exileThenSeek =
                (ExileSameNameCardsFromHandAndLibraryThenSeekEffect) effect;
        if (exileThenSeek.cardName() == null) {
            entry.setTargetId(entry.getControllerId());
            entry.setNonTargeting(true);
            playerInteractionSupport.resolveHandRevealAndChooseWithChosenCardThen(
                    gameData, entry, 1, List.of(), List.of(CardType.INSTANT, CardType.SORCERY),
                    null, false, true, null, true, false, 0, false,
                    false, false, false, 0, null, exileThenSeek);
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> matchingCards = matchingCards(gameData, controllerId, exileThenSeek.cardName());
        if (matchingCards.isEmpty()) {
            LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
            return;
        }

        playerInputService.beginMultiZoneExileChoice(
                gameData, controllerId, matchingCards, controllerId, exileThenSeek.cardName(), false,
                new SeekCardsToHandEffect(new EventValue(), exileThenSeek.seekFilter()));
    }

    private List<Card> matchingCards(GameData gameData, UUID controllerId, String cardName) {
        List<Card> matchingCards = new ArrayList<>();
        List<Card> hand = gameData.playerHands.get(controllerId);
        if (hand != null) {
            matchingCards.addAll(hand.stream().filter(card -> card.getName().equals(cardName)).toList());
        }
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (library != null) {
            matchingCards.addAll(library.stream().filter(card -> card.getName().equals(cardName)).toList());
        }
        return matchingCards;
    }
}
