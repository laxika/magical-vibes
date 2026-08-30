package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastExiledCardWithoutPayingManaCostEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Delegates an internal discard follow-up to the shared free-cast-from-exile support.
 */
@Component
@RequiredArgsConstructor
public class CastExiledCardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastExiledCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastExiledCardWithoutPayingManaCostEffect castEffect =
                (CastExiledCardWithoutPayingManaCostEffect) effect;
        Player player = new Player(entry.getControllerId(), gameData.playerIdToName.get(entry.getControllerId()));
        exileFreeCastSupport.castFromExileWithoutPaying(gameData, player, castEffect.exiledCardId());
    }
}
