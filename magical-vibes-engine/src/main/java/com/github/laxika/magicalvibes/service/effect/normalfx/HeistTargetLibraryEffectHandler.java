package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HeistTargetLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class HeistTargetLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HeistTargetLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID libraryOwnerId = entry.getTargetId();
        if (libraryOwnerId == null) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(libraryOwnerId);
        if (library == null || library.isEmpty()) {
            return;
        }

        List<Card> nonlandCards = library.stream()
                .filter(card -> !card.hasType(CardType.LAND))
                .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
        if (nonlandCards.isEmpty()) {
            return;
        }

        Collections.shuffle(nonlandCards);
        List<Card> choices = List.copyOf(nonlandCards.subList(0, Math.min(3, nonlandCards.size())));
        interactionHandlerRegistry.begin(gameData,
                new PendingInteraction.HeistCardChoice(entry.getControllerId(), libraryOwnerId, choices));
    }
}
