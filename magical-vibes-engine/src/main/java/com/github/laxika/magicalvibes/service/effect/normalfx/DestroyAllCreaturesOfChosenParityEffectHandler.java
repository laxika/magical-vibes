package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesOfChosenParityEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a creature destruction effect using the parity chosen earlier in the resolution. */
@Component
@RequiredArgsConstructor
public class DestroyAllCreaturesOfChosenParityEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllCreaturesOfChosenParityEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ManaValueParity chosen = gameData.chosenSpellManaValueParity;
        if (chosen == null) {
            return;
        }
        gameData.chosenSpellManaValueParity = null;

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && chosen.matches(permanent.getCard().getManaValue())) {
                    toDestroy.add(permanent);
                }
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), false);
    }
}
