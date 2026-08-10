package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaToActivePlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link AwardManaToActivePlayerEffect} — Eladamri's Vineyard's "that player adds
 * {@code {G}{G}}". The mana goes to the player recorded as the entry's target (the active player
 * at trigger time), falling back to the entry's controller when no target was recorded.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaToActivePlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaToActivePlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardManaToActivePlayerEffect) effect;

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
        pool.add(e.color(), amount);

        String playerName = gameData.playerIdToName.get(recipientId);
        gameLogService.append(gameData, GameLog.text(playerName + " adds " + amount + " " + e.color().getCode() + "."));
        log.info("Game {} - {} adds {} {}", gameData.id, playerName, amount, e.color());
    }
}
