package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.effect.AsksDeathReplacementsFirst;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import com.github.laxika.magicalvibes.service.effect.SubjectResolutionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.LinkedHashMap;

/**
 * Resolves {@link EachPlayerLosesFractionOfLifeRoundedUpEffect}: asks {@link SubjectResolutionService} for the
 * players, computes every loss from the current life totals and applies them as one {@link LifeSupport#applyLifeLosses}
 * map. A subgame result the entry carries (Shahrazad) is consumed here.
 */
@Component
@RequiredArgsConstructor
public class EachPlayerLosesFractionOfLifeRoundedUpEffectHandler implements NormalEffectHandlerBean, AsksDeathReplacementsFirst {

    private final LifeSupport lifeSupport;
    private final SubjectResolutionService subjectResolutionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesFractionOfLifeRoundedUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesFractionOfLifeRoundedUpEffect) effect;
        var amounts = new LinkedHashMap<UUID, Integer>();
        for (UUID playerId : subjectResolutionService.players(gameData, entry, effect, e.player())) {
            int currentLife = gameData.getLife(playerId);
            int lifeLoss = currentLife <= 0 ? 0 : (int) (((long) currentLife + e.divisor() - 1) / e.divisor());
            amounts.put(playerId, lifeLoss);
        }
        lifeSupport.applyLifeLosses(gameData, amounts, entry.getCard().getName());
        entry.setSubgameResult(null);
    }
}
