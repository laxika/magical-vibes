package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DamageSourceControllerMillsEffect;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Mills the damage source's controller (Belltower Sphinx). An unbound marker (no player id) resolves
 * as a no-op.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageSourceControllerMillsEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DamageSourceControllerMillsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DamageSourceControllerMillsEffect) effect;
        UUID playerId = e.millingPlayerId();
        if (playerId == null || e.count() <= 0 || !gameData.playerIds.contains(playerId)) return;

        graveyardService.resolveMillPlayer(gameData, playerId, e.count());
        log.info("Game {} - {} mills {} cards from {}", gameData.id,
                gameData.playerIdToName.get(playerId), e.count(), entry.getCard().getName());
    }
}
