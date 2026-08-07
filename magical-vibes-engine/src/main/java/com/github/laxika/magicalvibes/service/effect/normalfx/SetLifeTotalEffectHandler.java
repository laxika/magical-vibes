package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Resolves the whole "life total becomes" family via {@link SetLifeTotalEffect}: the
 * {@code SetLifeTotalRecipient} routes whose total is set and the {@code DynamicAmount} computes the
 * new value. Every path ends in {@link LifeSupport#applySetLifeTotal}, which implements CR 119.5 —
 * the player gains or loses the necessary amount, so "can't change life" / "can't gain life" and the
 * gain/loss triggers all apply.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SetLifeTotalEffectHandler implements NormalEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetLifeTotalEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetLifeTotalEffect) effect;

        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. a sacrificed source).
        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext context = AmountContext.forStackEntry(entry, source);

        switch (e.recipient()) {
            case CONTROLLER -> setOnePlayer(gameData, entry.getControllerId(), evaluate(gameData, e, context));
            // null target: "up to one target player" with none chosen (Torgaar), or a fizzled entry.
            case TARGET_PLAYER -> setOnePlayer(gameData, entry.getTargetId(), evaluate(gameData, e, context));
            case EACH_PLAYER -> setEachPlayer(gameData, e, context);
        }
    }

    private void setOnePlayer(GameData gameData, UUID playerId, int newLife) {
        if (playerId == null) return;
        applyAndLog(gameData, playerId, newLife);
    }

    /**
     * Each player's amount is evaluated from that player's own point of view (so a
     * {@code CountScope.CONTROLLER} amount reads "the creatures <em>they</em> control", Biorhythm),
     * and every new total is determined before any of them is applied — so the first player's change
     * cannot feed back into a later player's amount (Arbiter of Knollridge's "highest life total
     * among all players").
     */
    private void setEachPlayer(GameData gameData, SetLifeTotalEffect e, AmountContext context) {
        Map<UUID, Integer> newLifeTotals = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            newLifeTotals.put(playerId, evaluate(gameData, e, context.withControllerId(playerId)));
        }
        newLifeTotals.forEach((playerId, newLife) -> applyAndLog(gameData, playerId, newLife));
    }

    private int evaluate(GameData gameData, SetLifeTotalEffect e, AmountContext context) {
        return Math.max(0, amountEvaluationService.evaluate(gameData, e.amount(), context));
    }

    private void applyAndLog(GameData gameData, UUID playerId, int newLife) {
        int currentLife = gameData.getLife(playerId);
        if (lifeSupport.applySetLifeTotal(gameData, playerId, newLife) && currentLife != newLife) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(
                    playerName + "'s life total becomes " + newLife + " (was " + currentLife + ")."));
            log.info("Game {} - {}'s life set to {} (was {})", gameData.id, playerName, newLife, currentLife);
        }
    }
}
