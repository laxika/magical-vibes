package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastTargetCardFromGraveyardIfNoSpellThisTurnEffect;
import com.github.laxika.magicalvibes.service.input.MayCastHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CastTargetCardFromGraveyardIfNoSpellThisTurnHandler implements MayEffectHandlerBean {

    private final MayCastHandlerService mayCastHandlerService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CastTargetCardFromGraveyardIfNoSpellThisTurnEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        CastTargetCardFromGraveyardIfNoSpellThisTurnEffect castEffect = ability.effects().stream()
                .filter(e -> e instanceof CastTargetCardFromGraveyardIfNoSpellThisTurnEffect)
                .map(e -> (CastTargetCardFromGraveyardIfNoSpellThisTurnEffect) e)
                .findFirst().orElse(null);
        if (castEffect != null) {
            mayCastHandlerService.handleCastCardFromGraveyardIfNoSpellThisTurnChoice(
                    gameData, player, accepted, ability, castEffect);
        }
    }
}
