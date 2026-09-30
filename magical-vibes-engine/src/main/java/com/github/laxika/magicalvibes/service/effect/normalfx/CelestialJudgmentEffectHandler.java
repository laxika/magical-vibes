package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CelestialJudgmentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CelestialJudgmentEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CelestialJudgmentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Map<Integer, List<UUID>> creatureIdsByPower = collectCreatureIdsByPower(gameData);
        if (creatureIdsByPower.isEmpty()) {
            return;
        }

        chooseForPower(gameData, entry.getControllerId(), List.copyOf(creatureIdsByPower.keySet()),
                creatureIdsByPower, 0, List.of(), entry.getCard().getName());
    }

    public void completeChoice(GameData gameData, UUID permanentId,
                               PermanentChoiceContext.CelestialJudgmentChoice context) {
        List<UUID> chosenIds = new ArrayList<>(context.chosenIds());
        chosenIds.add(permanentId);
        int nextPowerIndex = context.powerIndex() + 1;
        if (nextPowerIndex >= context.powers().size()) {
            destructionSupport.performDestroyAllCreaturesExcept(gameData, context.sourceName(), chosenIds);
            return;
        }

        chooseForPower(gameData, gameData.pendingEffectResolutionEntry.getControllerId(), context.powers(),
                collectCreatureIdsByPower(gameData), nextPowerIndex, chosenIds, context.sourceName());
    }

    private void chooseForPower(GameData gameData, UUID controllerId, List<Integer> powers,
                                Map<Integer, List<UUID>> creatureIdsByPower, int powerIndex,
                                List<UUID> chosenIds, String sourceName) {
        int power = powers.get(powerIndex);
        List<UUID> candidates = creatureIdsByPower.getOrDefault(power, List.of());
        if (candidates.isEmpty()) {
            destructionSupport.performDestroyAllCreaturesExcept(gameData, sourceName, chosenIds);
            return;
        }
        if (candidates.size() == 1) {
            List<UUID> updatedChosenIds = new ArrayList<>(chosenIds);
            updatedChosenIds.add(candidates.getFirst());
            if (powerIndex + 1 >= powers.size()) {
                destructionSupport.performDestroyAllCreaturesExcept(gameData, sourceName, updatedChosenIds);
            } else {
                chooseForPower(gameData, controllerId, powers, creatureIdsByPower, powerIndex + 1,
                        updatedChosenIds, sourceName);
            }
            return;
        }

        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                candidates,
                new PermanentChoiceContext.CelestialJudgmentChoice(powers, powerIndex, chosenIds, sourceName),
                sourceName + " — choose a creature with power " + power + ".");
    }

    private Map<Integer, List<UUID>> collectCreatureIdsByPower(GameData gameData) {
        Map<Integer, List<UUID>> creatureIdsByPower = new TreeMap<>();
        gameData.forEachBattlefield((playerId, battlefield) -> {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.isCreature(gameData, permanent)) {
                    creatureIdsByPower.computeIfAbsent(
                            gameQueryService.getEffectivePower(gameData, permanent), ignored -> new ArrayList<>())
                            .add(permanent.getId());
                }
            }
        });
        return creatureIdsByPower;
    }
}
