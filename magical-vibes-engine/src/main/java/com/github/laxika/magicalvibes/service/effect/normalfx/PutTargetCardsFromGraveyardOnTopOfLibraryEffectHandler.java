package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PutTargetCardsFromGraveyardOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutTargetCardsFromGraveyardOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (PutTargetCardsFromGraveyardOnTopOfLibraryEffect) effect;

        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        graveyardReturnSupport.processTargetedGraveyardCards(gameData, entry,
                (graveyard, card) -> library.addFirst(card),
                " puts ", " on top of their library from graveyard.");
    }
}
