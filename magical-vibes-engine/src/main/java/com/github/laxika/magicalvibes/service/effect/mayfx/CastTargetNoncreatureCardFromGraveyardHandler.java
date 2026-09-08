package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetNoncreatureCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Free noncreature cast from the controller's graveyard, as used by Vadrok.
 */
@Component
@RequiredArgsConstructor
public class CastTargetNoncreatureCardFromGraveyardHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetNoncreatureCardFromGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastTargetNoncreatureCardFromGraveyardEffect castFromGraveyardEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTargetNoncreatureCardFromGraveyardEffect)
                .map(e -> (CastTargetNoncreatureCardFromGraveyardEffect) e)
                .findFirst().orElse(null);
        if (castFromGraveyardEffect != null) {
            mayCastHandlerService.handleCastFromNoncreatureGraveyardChoice(
                    gameData, player, accepted, ability, castFromGraveyardEffect);
        }
    }
}
