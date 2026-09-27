package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCreaturesOfChosenPowerParityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a mass bounce using the effective power parity chosen during resolution. */
@Component
@RequiredArgsConstructor
public class ReturnAllCreaturesOfChosenPowerParityEffectHandler implements NormalEffectHandlerBean {

    private final BounceSupport bounceSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnAllCreaturesOfChosenPowerParityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManaValueParity chosen = gameData.chosenSpellManaValueParity;
        if (chosen == null) {
            return;
        }
        gameData.chosenSpellManaValueParity = null;

        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                toReturn.addAll(battlefield.stream()
                        .filter(permanent -> gameQueryService.isCreature(gameData, permanent))
                        .filter(permanent -> chosen.matches(gameQueryService.getEffectivePower(gameData, permanent)))
                        .toList()));

        bounceSupport.applyReturnPermanentsToHand(gameData, entry, toReturn);
    }
}
