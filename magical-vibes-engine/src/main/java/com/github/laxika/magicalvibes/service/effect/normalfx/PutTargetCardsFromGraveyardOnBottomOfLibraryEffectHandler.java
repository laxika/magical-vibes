package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnBottomOfLibraryEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardsFromGraveyardOnBottomOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardsFromGraveyardOnBottomOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var library = gameData.playerDecks.get(entry.getControllerId());
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> library.addLast(card),
                " puts ", " on the bottom of their library from graveyard.");
    }
}
