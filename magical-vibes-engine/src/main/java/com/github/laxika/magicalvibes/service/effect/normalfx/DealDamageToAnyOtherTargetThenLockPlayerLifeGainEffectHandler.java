package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffectHandler
        implements NormalEffectHandlerBean {

    private final DealDamageToAnyTargetEffectHandler dealDamageHandler;
    private final TargetPlayerCantGainLifeRestOfGameEffectHandler lifeGainLockHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var damageEffect = (DealDamageToAnyOtherTargetThenLockPlayerLifeGainEffect) effect;
        Map<UUID, Integer> damageBefore = new HashMap<>(gameData.damageDealtToPlayersThisTurn);

        dealDamageHandler.resolve(gameData, entry, new DealDamageToAnyTargetEffect(damageEffect.damage()));

        gameData.damageDealtToPlayersThisTurn.forEach((playerId, damageAfter) -> {
            if (damageAfter > damageBefore.getOrDefault(playerId, 0)) {
                lifeGainLockHandler.resolveForPlayer(gameData, playerId);
            }
        });
    }
}
