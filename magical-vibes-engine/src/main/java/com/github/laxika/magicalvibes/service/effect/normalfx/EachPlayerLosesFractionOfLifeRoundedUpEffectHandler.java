package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerLosesFractionOfLifeRoundedUpEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.LinkedHashMap;
import com.github.laxika.magicalvibes.model.effect.FractionalLifeLossRecipient;

@Component
@RequiredArgsConstructor
public class EachPlayerLosesFractionOfLifeRoundedUpEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerLosesFractionOfLifeRoundedUpEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerLosesFractionOfLifeRoundedUpEffect) effect;
        var amounts = new LinkedHashMap<UUID, Integer>();
        if (e.recipient() == FractionalLifeLossRecipient.SUBGAME_NON_WINNERS && entry.getSubgameResult() == null) {
            throw new IllegalStateException("Missing subgame result");
        }
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (e.opponentsOnly() && playerId.equals(entry.getControllerId())) {
                continue;
            }
            if (e.recipient() == FractionalLifeLossRecipient.SUBGAME_NON_WINNERS
                    && entry.getSubgameResult().winners().contains(playerId)) continue;
            int currentLife = gameData.getLife(playerId);
            int lifeLoss = currentLife <= 0 ? 0 : (int) (((long) currentLife + e.divisor() - 1) / e.divisor());
            amounts.put(playerId, lifeLoss);
        }
        amounts.forEach((playerId, lifeLoss) -> {
            if (lifeLoss > 0) {
                lifeSupport.applyLifeLoss(gameData, playerId, lifeLoss, entry.getCard().getName());
            }
        });
        if (e.recipient() == FractionalLifeLossRecipient.SUBGAME_NON_WINNERS) entry.setSubgameResult(null);
    }
}
