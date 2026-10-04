package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnBottomOfLibraryEffect;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardsFromGraveyardOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<Card> movedCards = new ArrayList<>();
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> movedCards.add(card),
                " puts ", " on the bottom of their library from graveyard.");
        if (movedCards.size() < 2) {
            gameData.playerDecks.get(entry.getControllerId()).addAll(movedCards);
            return;
        }
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.LibraryReorder(
                entry.getControllerId(), movedCards, true, entry.getControllerId(),
                "Put these cards on the bottom of your library in any order."));
    }
}
