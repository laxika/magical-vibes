package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MillDefendingPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link MillDefendingPlayerEffect} (Nemesis of Reason): the defending player captured when
 * the attack was declared (the attacked player, the controller of the attacked planeswalker, or the
 * protector of the attacked battle) mills {@code count} cards. The captured player is used so the
 * mill still happens if the attacked permanent has left the battlefield. Reuses
 * {@code GraveyardService.resolveMillPlayer}.
 */
@Component
@RequiredArgsConstructor
public class MillDefendingPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GraveyardService graveyardService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MillDefendingPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var mill = (MillDefendingPlayerEffect) effect;

        UUID attackedTargetId = entry.getAttackedTargetId();
        if (attackedTargetId == null) {
            return;
        }
        UUID defendingPlayerId = entry.getDefendingPlayerId() != null
                ? entry.getDefendingPlayerId()
                : gameData.playerIds.contains(attackedTargetId)
                        ? attackedTargetId
                        : gameQueryService.findPermanentController(gameData, attackedTargetId);
        if (defendingPlayerId != null) {
            graveyardService.resolveMillPlayer(gameData, defendingPlayerId, mill.count());
        }
    }
}
