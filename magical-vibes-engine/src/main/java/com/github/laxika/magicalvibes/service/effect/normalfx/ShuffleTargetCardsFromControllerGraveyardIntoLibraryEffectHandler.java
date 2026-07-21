package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.service.library.LibraryShuffleHelper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();
        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        List<Card> library = gameData.playerDecks.get(controllerId);
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> library.add(card),
                " shuffles ", " from graveyard into their library.");
        LibraryShuffleHelper.shuffleLibrary(gameData, controllerId);
    }
}
