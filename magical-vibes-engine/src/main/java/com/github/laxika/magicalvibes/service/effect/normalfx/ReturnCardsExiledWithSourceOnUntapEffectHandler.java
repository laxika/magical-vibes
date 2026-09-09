package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnCardsExiledWithSourceOnUntapEffect;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a source-linked exile return after the source's untap trigger resolves. */
@Component
@RequiredArgsConstructor
public class ReturnCardsExiledWithSourceOnUntapEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnCardsExiledWithSourceOnUntapEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() != null
                && effect instanceof ReturnCardsExiledWithSourceOnUntapEffect returnEffect) {
            permanentRemovalService.returnExileReturnsOnSourceEvent(
                    gameData, entry.getSourcePermanentId(), returnEffect.primaryCardIds());
        }
    }
}
