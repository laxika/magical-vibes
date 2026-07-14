package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetInstantOrSorceryFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Cast-from-graveyard — e.g. Chancellor of the Spires.
 */
@Component
@RequiredArgsConstructor
public class CastTargetInstantOrSorceryFromGraveyardHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetInstantOrSorceryFromGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastTargetInstantOrSorceryFromGraveyardEffect castFromGraveyardEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTargetInstantOrSorceryFromGraveyardEffect)
                .map(e -> (CastTargetInstantOrSorceryFromGraveyardEffect) e)
                .findFirst().orElse(null);
        if (castFromGraveyardEffect != null) {
            mayCastHandlerService.handleCastFromGraveyardChoice(gameData, player, accepted, ability, castFromGraveyardEffect);
        }
    }
}
