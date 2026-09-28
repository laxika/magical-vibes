package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoLibraryAtPositionEffect;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ConjureCardIntoLibraryAtPositionEffectHandler implements NormalEffectHandlerBean {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoLibraryAtPositionEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardIntoLibraryAtPositionEffect conjure =
                (ConjureCardIntoLibraryAtPositionEffect) effect;
        List<Card> library = gameData.playerDecks.get(entry.getControllerId());
        if (library == null) {
            return;
        }

        Card card = conjure.cardFactory().get();
        if (card == null) {
            return;
        }
        card.setOwnerId(entry.getControllerId());
        card.freeze();
        library.add(Math.min(conjure.position(), library.size()), card);
    }
}
