package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AddAnotherCounterOfChosenTypeToEachNonSagaPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves The Caves of Androzani's per-permanent counter choices. */
@Component
@RequiredArgsConstructor
public class AddAnotherCounterOfChosenTypeToEachNonSagaPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AddAnotherCounterOfChosenTypeToEachNonSagaPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> permanentIds = new ArrayList<>();
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (!permanent.getCard().isSaga() && !counterTypesOn(permanent).isEmpty()) {
                    permanentIds.add(permanent.getId());
                }
            }
        }

        beginNextChoice(gameData, entry, permanentIds);
    }

    private void beginNextChoice(GameData gameData, StackEntry entry, List<UUID> remainingIds) {
        while (!remainingIds.isEmpty()) {
            UUID targetId = remainingIds.removeFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || target.getCard().isSaga()) {
                continue;
            }

            List<CounterType> counterTypes = counterTypesOn(target);
            if (counterTypes.isEmpty()) {
                continue;
            }

            playerInputService.beginAddAnotherCounterTypeOnEachNonSagaPermanentChoice(
                    gameData, entry.getControllerId(), targetId, entry.getCard().getName(),
                    remainingIds, counterTypes);
            return;
        }
    }

    private static List<CounterType> counterTypesOn(Permanent permanent) {
        List<CounterType> counterTypes = new ArrayList<>();
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                    && permanent.getCounterCount(counterType) > 0) {
                counterTypes.add(counterType);
            }
        }
        return counterTypes;
    }
}
