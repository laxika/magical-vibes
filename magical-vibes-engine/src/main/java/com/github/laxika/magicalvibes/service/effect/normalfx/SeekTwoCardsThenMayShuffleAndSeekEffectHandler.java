package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SeekCardsToHandEffect;
import com.github.laxika.magicalvibes.model.effect.SeekTwoCardsThenMayShuffleAndSeekEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

/** Resolves Choice of Fortunes' repeatable Seek while retaining the exact first cards found. */
@Component
public class SeekTwoCardsThenMayShuffleAndSeekEffectHandler implements NormalEffectHandlerBean {

    private final SeekCardsToHandEffectHandler seekCardsToHandEffectHandler;

    public SeekTwoCardsThenMayShuffleAndSeekEffectHandler(
            SeekCardsToHandEffectHandler seekCardsToHandEffectHandler) {
        this.seekCardsToHandEffectHandler = seekCardsToHandEffectHandler;
    }

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekTwoCardsThenMayShuffleAndSeekEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        SeekTwoCardsThenMayShuffleAndSeekEffect seek =
                (SeekTwoCardsThenMayShuffleAndSeekEffect) effect;
        if (seek.soughtCards().isEmpty()) {
            List<Card> soughtCards = seekCardsToHandEffectHandler.seek(
                    gameData, entry, new SeekCardsToHandEffect(new Fixed(2), null));
            if (!soughtCards.isEmpty()) {
                entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, List.of(
                        new MayEffect(
                                new SeekTwoCardsThenMayShuffleAndSeekEffect(soughtCards),
                                "Shuffle them into your library?")));
            }
            return;
        }

        UUID controllerId = entry.getControllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        List<Card> library = gameData.playerDecks.get(controllerId);
        if (hand == null || library == null) {
            return;
        }

        List<Card> cardsToShuffle = seek.soughtCards().stream()
                .filter(sought -> hand.stream().anyMatch(card -> card.getId().equals(sought.getId())))
                .toList();
        if (cardsToShuffle.isEmpty()) {
            return;
        }

        for (Card card : cardsToShuffle) {
            hand.removeIf(handCard -> handCard.getId().equals(card.getId()));
            library.add(card);
        }
        Collections.shuffle(library);

        entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1,
                List.of(new SeekCardsToHandEffect(new Fixed(2), null)));
    }
}
