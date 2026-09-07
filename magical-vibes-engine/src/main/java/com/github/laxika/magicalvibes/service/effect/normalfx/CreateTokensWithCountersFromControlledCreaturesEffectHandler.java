package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensWithCountersFromControlledCreaturesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokensWithCountersFromControlledCreaturesEffectHandler implements NormalEffectHandlerBean {

    private final CreateTokenEffectHandler createTokenEffectHandler;
    private final GameQueryService gameQueryService;
    private final PutCounterOnEitherTargetPermanentEffectHandler putCounterHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokensWithCountersFromControlledCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokensWithCountersFromControlledCreaturesEffect createAndCounter =
                (CreateTokensWithCountersFromControlledCreaturesEffect) effect;
        int createdBefore = entry.getCreatedPermanentIds().size();

        createTokenEffectHandler.resolve(gameData, entry, createAndCounter.tokenTemplate());
        if (gameData.resolvingMayEffectFromStack || !gameData.pendingMayAbilities.isEmpty()) {
            return;
        }
        if (entry.getCreatedPermanentIds().size() == createdBefore) {
            return;
        }

        List<CounterType> counterTypes = counterTypesAmongControlledCreatures(gameData, entry);
        if (!counterTypes.isEmpty()) {
            List<UUID> createdTokenIds = entry.getCreatedPermanentIds()
                    .subList(createdBefore, entry.getCreatedPermanentIds().size());
            putCounterHandler.beginCounterPlacement(gameData, entry, createdTokenIds, counterTypes);
        }
    }

    private List<CounterType> counterTypesAmongControlledCreatures(GameData gameData, StackEntry entry) {
        EnumSet<CounterType> counterTypes = EnumSet.noneOf(CounterType.class);
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(entry.getControllerId(), List.of())) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            for (CounterType counterType : CounterType.values()) {
                if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                        && permanent.getCounterCount(counterType) > 0) {
                    counterTypes.add(counterType);
                }
            }
        }
        return List.copyOf(counterTypes);
    }
}
