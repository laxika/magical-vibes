package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillHalfDefendingPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Terisian Mindbreaker's non-targeting attack trigger. */
@Component
@RequiredArgsConstructor
public class MillHalfDefendingPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillHalfDefendingPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return;
        }
        UUID defendingPlayerId = gameData.playerIds.contains(attackedTargetId)
                ? attackedTargetId
                : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId == null) {
            return;
        }

        int librarySize = gameData.playerDecks.get(defendingPlayerId).size();
        graveyardService.resolveMillPlayer(gameData, defendingPlayerId, (librarySize + 1) / 2);
    }
}
