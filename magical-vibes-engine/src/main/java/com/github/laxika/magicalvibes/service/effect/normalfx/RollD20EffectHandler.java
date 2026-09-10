package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.RollD20Effect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RollD20EffectHandler implements NormalEffectHandlerBean {

    private final GameLogService gameLogService;
    private final D20RollService d20RollService;
    private final TriggerCollectionService triggerCollectionService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RollD20Effect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        RollD20Effect rollEffect = (RollD20Effect) effect;
        int rawResult = d20RollService.roll(gameData, entry.getControllerId());
        gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(entry.getControllerId())
                + " rolls a d20 for " + entry.getCard().getName() + ": " + rawResult + "."));
        triggerCollectionService.checkControllerRollsOneOrMoreDiceTriggers(
                gameData, entry.getControllerId(), 1, rawResult);
        if (rawResult == 20) {
            triggerCollectionService.checkControllerRollsNaturalTwentyTriggers(gameData, entry.getControllerId());
        }

        int result = rawResult;
        if (rollEffect.amountToSubtract() != null) {
            var source = entry.getSourcePermanentId() == null ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            int amount = amountEvaluationService.evaluate(gameData, rollEffect.amountToSubtract(),
                    AmountContext.forStackEntry(entry, source));
            result -= amount;
        }

        boolean isTwenty = (rollEffect.twentyUsesRawResult() ? rawResult : result) == 20;
        CardEffect branch = result <= rollEffect.firstBranchMax()
                ? result <= 0 ? rollEffect.zeroOrLess() : rollEffect.oneToNine()
                : isTwenty && rollEffect.twenty() != null
                        ? rollEffect.twenty()
                        : rollEffect.tenToNineteen();
        if (branch == null) {
            return;
        }

        int effectIndex = -1;
        for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
            if (entry.getEffectsToResolve().get(i) == effect) {
                effectIndex = i;
                break;
            }
        }
        if (effectIndex < 0) {
            for (int i = 0; i < entry.getEffectsToResolve().size(); i++) {
                CardEffect current = entry.getEffectsToResolve().get(i);
                if (current instanceof MayEffect may && may.wrapped() == effect) {
                    effectIndex = i;
                    break;
                }
            }
        }
        if (effectIndex >= 0) {
            if (rollEffect.repeatOnHighBranch() && result > rollEffect.firstBranchMax()) {
                entry.insertEffectsToResolve(effectIndex + 1,
                        List.of(branch, new MayEffect(rollEffect.copyForRepeat(), "Roll again?")));
            } else {
                entry.insertEffectsToResolve(effectIndex + 1, List.of(branch));
            }
        }
    }
}
