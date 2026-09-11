package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CastCardFromGraveyardHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastCardFromGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastCardFromGraveyardEffect castEffect = ability.effects().stream()
                .filter(CastCardFromGraveyardEffect.class::isInstance)
                .map(CastCardFromGraveyardEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (castEffect != null) {
            mayCastHandlerService.handleCastCardFromGraveyardChoice(
                    gameData, player, accepted, ability, castEffect);
        }
    }
}
