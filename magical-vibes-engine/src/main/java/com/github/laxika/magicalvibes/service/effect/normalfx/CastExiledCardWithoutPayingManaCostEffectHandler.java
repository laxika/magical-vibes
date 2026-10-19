package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Casts an exiled card through the shared support during the surrounding effect's resolution.
 */
@Component
@RequiredArgsConstructor
public class CastExiledCardWithoutPayingManaCostEffectHandler implements NormalEffectHandlerBean {

    private final ExileFreeCastSupport exileFreeCastSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastExiledCardWithoutPayingManaCostEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CastExiledCardWithoutPayingManaCostEffect castEffect =
                (CastExiledCardWithoutPayingManaCostEffect) effect;
        // A suspend cast is still a cast: an opponent locked to sorcery timing (Teferi, Mage of Zhalfir)
        // can't make it from a resolving trigger outside their own main phase.
        if (castEffect.suspendHaste()
                && gameQueryService.isLockedOutByOpponentsSorceryTimingRestriction(gameData, entry.getControllerId())) {
            return;
        }
        Player player = new Player(entry.getControllerId(), gameData.playerIdToName.get(entry.getControllerId()));
        exileFreeCastSupport.castFromExileWithoutPaying(
                gameData, player, castEffect.exiledCardId(), castEffect.grantHaste(), false,
                castEffect.suspendHaste(), false);
    }
}
