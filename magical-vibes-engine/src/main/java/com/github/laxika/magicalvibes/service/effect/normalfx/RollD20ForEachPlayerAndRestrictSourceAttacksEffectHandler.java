package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreaturesCantAttackControllerUnlessPredicateEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.RollD20ForEachPlayerAndRestrictSourceAttacksEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RollD20ForEachPlayerAndRestrictSourceAttacksEffectHandler implements NormalEffectHandlerBean {

    private final D20RollService d20RollService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD20ForEachPlayerAndRestrictSourceAttacksEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        Map<UUID, Integer> results = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            int result = d20RollService.roll(gameData, playerId);
            results.put(playerId, result);
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.getOrDefault(playerId, "A player")
                    + " rolls a d20 for " + entry.getCard().getName() + ": " + result + "."));
            triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                    gameData, playerId, 1, result);
            triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                    gameData, playerId, 20, result);
            if (result == 20) {
                triggerCollectionService.checkControllerRollsNaturalTwentyTriggers(gameData, playerId);
            }
        }

        if (controllerId == null || results.isEmpty() || entry.getSourcePermanentId() == null) {
            return;
        }

        int highestResult = results.values().stream().mapToInt(Integer::intValue).max().orElse(0);
        for (Map.Entry<UUID, Integer> result : results.entrySet()) {
            UUID opponentId = result.getKey();
            if (controllerId.equals(opponentId) || result.getValue() != highestResult) {
                continue;
            }

            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard().getName(), entry.getSourcePermanentId(), controllerId,
                    new CreaturesCantAttackControllerUnlessPredicateEffect(
                            new PermanentNotPredicate(new PermanentTruePredicate()), true),
                    entry.getSourcePermanentId(), opponentId, null, EffectDuration.UNTIL_END_OF_COMBAT, 0L));
        }
    }
}
