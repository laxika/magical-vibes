package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsFromControllerGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class PutCardsFromControllerGraveyardOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutCardsFromControllerGraveyardOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutCardsFromControllerGraveyardOnTopOfLibraryEffect) effect;
        List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
        if (graveyard == null || graveyard.isEmpty() || e.maxCount() <= 0) {
            return;
        }

        List<Card> availableCards = new ArrayList<>(graveyard);
        int count = Math.min(e.maxCount(), availableCards.size());
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                entry.getControllerId(), availableCards, count,
                "Choose " + count + " cards from your graveyard to put on top of your library.",
                count, true));
    }
}
