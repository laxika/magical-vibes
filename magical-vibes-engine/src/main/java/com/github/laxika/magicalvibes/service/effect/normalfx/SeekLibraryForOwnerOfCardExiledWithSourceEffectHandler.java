package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryForOwnerOfCardExiledWithSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves a source-leaves Seek for the owner of the source's tracked exiled card. */
@Component
@RequiredArgsConstructor
public class SeekLibraryForOwnerOfCardExiledWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryForOwnerOfCardExiledWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null) {
            return;
        }

        ExiledCardEntry exiled = gameData.exiledCards.stream()
                .filter(candidate -> sourcePermanentId.equals(candidate.sourcePermanentId()))
                .findFirst()
                .orElse(null);
        if (exiled == null || exiled.ownerId() == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(exiled.ownerId());
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> matchingCards = new ArrayList<>(library.stream()
                .filter(card -> sharesCardType(card, exiled.card(), gameData, exiled.ownerId()))
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card chosen = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.removeIf(card -> card.getId().equals(chosen.getId()));
        gameData.addCardToHand(exiled.ownerId(), chosen);
    }

    private boolean sharesCardType(Card candidate, Card exiledCard, GameData gameData, UUID ownerId) {
        return java.util.Arrays.stream(CardType.values())
                .anyMatch(type -> gameQueryService.cardHasType(candidate, type, gameData, ownerId)
                        && gameQueryService.cardHasType(exiledCard, type, gameData, ownerId));
    }
}
