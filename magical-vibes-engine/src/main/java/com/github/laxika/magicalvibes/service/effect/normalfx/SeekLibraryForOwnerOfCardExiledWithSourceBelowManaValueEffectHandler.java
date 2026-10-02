package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SeekLibraryForOwnerOfCardExiledWithSourceBelowManaValueEffect;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/** Resolves Cackling Observer's source-leaves lesser-mana-value Seek. */
@Component
@RequiredArgsConstructor
public class SeekLibraryForOwnerOfCardExiledWithSourceBelowManaValueEffectHandler
        implements NormalEffectHandlerBean {

    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SeekLibraryForOwnerOfCardExiledWithSourceBelowManaValueEffect.class;
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
                .filter(card -> !card.hasType(CardType.LAND))
                .filter(card -> card.getManaValue() < exiled.card().getManaValue())
                .toList());
        if (matchingCards.isEmpty()) {
            return;
        }

        Card chosen = matchingCards.get(ThreadLocalRandom.current().nextInt(matchingCards.size()));
        library.removeIf(card -> card.getId().equals(chosen.getId()));
        gameData.addCardToHand(exiled.ownerId(), chosen);
        triggerCollectionService.checkControllerCardPutIntoHandFromLibraryTriggers(
                gameData, exiled.ownerId(), chosen);
        triggerCollectionService.checkSeekTriggers(gameData, exiled.ownerId(), List.of(chosen));
    }
}
