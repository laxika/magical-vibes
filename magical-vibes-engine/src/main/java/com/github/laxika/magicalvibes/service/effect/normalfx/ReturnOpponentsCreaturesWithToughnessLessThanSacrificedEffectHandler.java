package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Profaner of the Dead's exploit-triggered mass bounce. */
@Component
@RequiredArgsConstructor
public class ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffectHandler
        implements NormalEffectHandlerBean {

    private final BounceSupport bounceSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnOpponentsCreaturesWithToughnessLessThanSacrificedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        int sacrificedToughness = entry.getSacrificedToughness();
        List<Permanent> toReturn = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            if (entry.getControllerId().equals(playerId)) {
                return;
            }
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && gameQueryService.getEffectiveToughness(gameData, permanent) < sacrificedToughness) {
                    toReturn.add(permanent);
                }
            }
        });
        bounceSupport.applyReturnPermanentsToHand(gameData, entry, toReturn);
    }
}
