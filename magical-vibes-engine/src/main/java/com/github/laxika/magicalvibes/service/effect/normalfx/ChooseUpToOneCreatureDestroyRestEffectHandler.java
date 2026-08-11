package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseUpToOneCreatureDestroyRestEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseUpToOneCreatureDestroyRestEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseUpToOneCreatureDestroyRestEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> creatureIds = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureIds.add(permanent.getId());
                }
            }
        });

        if (creatureIds.isEmpty()) {
            destructionSupport.performDestroyAllCreaturesExcept(gameData, entry.getCard().getName(), List.of());
            return;
        }

        destructionSupport.beginNextDestroyRestChoice(
                gameData,
                List.of(new PendingForcedSacrifice(entry.getControllerId(), 1, List.copyOf(creatureIds))),
                List.of(),
                entry.getCard().getName());
    }
}
