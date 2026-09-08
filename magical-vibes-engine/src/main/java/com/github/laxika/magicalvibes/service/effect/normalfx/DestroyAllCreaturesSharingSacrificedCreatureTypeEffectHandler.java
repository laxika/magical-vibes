package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesSharingSacrificedCreatureTypeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Endemic Plague using the creature sacrificed to cast it. */
@Component
@RequiredArgsConstructor
public class DestroyAllCreaturesSharingSacrificedCreatureTypeEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final DestructionSupport destructionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyAllCreaturesSharingSacrificedCreatureTypeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sacrificed = entry.getSacrificedCardSnapshot();
        if (sacrificed == null) {
            return;
        }

        List<Permanent> toDestroy = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)
                        && gameQueryService.shareCreatureType(gameData, permanent, sacrificed)) {
                    toDestroy.add(permanent);
                }
            }
        });

        destructionSupport.destroyBatch(gameData, toDestroy, entry.getCard().getName(), true);
    }
}
