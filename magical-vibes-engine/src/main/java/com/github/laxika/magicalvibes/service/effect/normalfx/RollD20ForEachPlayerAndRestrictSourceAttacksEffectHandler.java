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
import com.github.laxika.magicalvibes.service.effect.ChoosesCoinFlipResultsFirst;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Each player rolls a d20; the source can't attack the opponents with the highest result this combat. Every roll is
 * made before anything is logged or triggers, and the rolls Pokey, the Scallywagg replaces with coin flips are
 * flipped before any d20, so it is marked {@link ChoosesCoinFlipResultsFirst} for Krark's Thumb.
 */
@Component
@RequiredArgsConstructor
public class RollD20ForEachPlayerAndRestrictSourceAttacksEffectHandler implements NormalEffectHandlerBean,
        ChoosesCoinFlipResultsFirst {

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
        List<UUID> playerIds = List.copyOf(gameData.orderedPlayerIds);
        List<D20RollService.D20RollResult> rolls = d20RollService.rollEach(gameData, playerIds);
        Map<UUID, Integer> results = new LinkedHashMap<>();
        for (int i = 0; i < playerIds.size(); i++) {
            UUID playerId = playerIds.get(i);
            D20RollService.D20RollResult roll = rolls.get(i);
            int result = roll.result();
            results.put(playerId, result);
            gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.getOrDefault(playerId, "A player")
                    + (roll.wasD20Roll() ? " rolls a d20 for " : " flips a coin for ")
                    + entry.getCard().getName() + ": " + result + "."));
            if (roll.wasD20Roll()) {
                triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                        gameData, playerId, 1, result);
                triggerCollectionService.checkControllerRollsHighestNaturalResultTriggers(
                        gameData, playerId, 20, result);
                if (result == 20) {
                    triggerCollectionService.checkControllerRollsNaturalTwentyTriggers(gameData, playerId);
                }
            } else if (result == 20) {
                triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, playerId);
            } else {
                triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, playerId);
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
