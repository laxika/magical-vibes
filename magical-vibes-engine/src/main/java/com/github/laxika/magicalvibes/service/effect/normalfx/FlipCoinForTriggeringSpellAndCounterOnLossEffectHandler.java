package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.FlipCoinForTriggeringSpellAndCounterOnLossEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.EffectHandler;
import com.github.laxika.magicalvibes.service.effect.EffectHandlerRegistry;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FlipCoinForTriggeringSpellAndCounterOnLossEffectHandler implements NormalEffectHandlerBean {

    private final EffectHandlerRegistry effectHandlerRegistry;
    private final GameLogService gameLogService;
    private final CoinFlipService coinFlipService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return FlipCoinForTriggeringSpellAndCounterOnLossEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID spellId = entry.getTargetId();
        if (spellId == null) return;

        StackEntry triggeringSpell = gameData.stack.stream()
                .filter(stackEntry -> stackEntry.getTargetableId().equals(spellId))
                .findFirst()
                .orElse(null);
        if (triggeringSpell == null) return;

        UUID casterId = triggeringSpell.getControllerId();
        CoinFlipService.CoinFlipResult result = coinFlipService.flip(gameData, casterId);
        String sourceName = entry.getCard().getName();
        String resultText = result.heads() ? " wins " : " loses ";
        gameLogService.append(gameData, GameLog.text(
                gameData.playerIdToName.get(casterId) + resultText + "the coin flip for " + sourceName
                        + coinFlipService.replacementDetails(result) + "."));

        if (result.heads()) {
            triggerCollectionService.checkControllerWinsCoinFlipTriggers(gameData, casterId);
            return;
        }

        triggerCollectionService.checkControllerLosesCoinFlipTriggers(gameData, casterId);
        EffectHandler counterHandler = effectHandlerRegistry.getHandler(new CounterSpellEffect());
        if (counterHandler != null) {
            counterHandler.resolve(gameData, entry, new CounterSpellEffect());
        } else {
            log.warn("No handler for CounterSpellEffect used by Mirrored Depths");
        }
    }
}
