package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaToCastingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ManaProductionSupport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves mana granted to the player who cast the triggering spell. */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaToCastingPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaToCastingPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardManaToCastingPlayerEffect) effect;
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }

        int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                AmountContext.forStackEntry(entry, source));
        if (amount <= 0) {
            return;
        }

        UUID recipientId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(recipientId);
        if (pool == null) {
            return;
        }
        ManaColor effectiveColor = ManaProductionSupport.effectiveColor(gameData, entry.getControllerId(), e.color());
        pool.add(effectiveColor, amount);

        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + amount + " " + effectiveColor.getCode() + "."));
        log.info("Game {} - {} adds {} {}", gameData.id, playerName, amount, effectiveColor);
    }
}
