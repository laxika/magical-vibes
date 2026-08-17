package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinDestroyTargetOrRepeatEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FlipCoinDestroyTargetOrRepeatEffectHandler implements NormalEffectHandlerBean {

    private final CoinFlipService coinFlipService;
    private final DestroyTargetPermanentEffectHandler destroyTargetPermanentEffectHandler;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinDestroyTargetOrRepeatEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var flipEffect = (FlipCoinDestroyTargetOrRepeatEffect) effect;
        UUID controllerId = entry.getControllerId();
        String sourceName = entry.getCard().getName();
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, controllerId);
        boolean wonFlip = result.heads();

        String flipLog = wonFlip
                ? gameData.playerIdToName.get(controllerId) + " wins the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + "."
                : gameData.playerIdToName.get(controllerId) + " loses the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + ".";
        gameLogService.append(gameData, GameLog.text(flipLog));

        if (wonFlip) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, controllerId);
            destroyTargetPermanentEffectHandler.resolve(
                    gameData, entry, DestroyTargetPermanentEffect.forTargetGroup(1));
            return;
        }

        MayPayManaEffect repeat = new MayPayManaEffect(
                "{3}",
                flipEffect,
                "Pay {3} to repeat this process?",
                DestroyTargetPermanentEffect.forTargetGroup(0));
        int effectIndex = findEffectIndex(entry, flipEffect);
        entry.insertEffectsToResolve(effectIndex + 1, List.of(repeat));
    }

    private int findEffectIndex(StackEntry entry, CardEffect effect) {
        List<CardEffect> effects = entry.getEffectsToResolve();
        for (int i = 0; i < effects.size(); i++) {
            if (effects.get(i) == effect) {
                return i;
            }
        }
        throw new IllegalStateException("FlipCoinDestroyTargetOrRepeatEffect is not resolving from its stack entry");
    }
}
