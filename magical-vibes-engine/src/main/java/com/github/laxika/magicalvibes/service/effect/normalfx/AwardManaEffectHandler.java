package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * Resolves {@link AwardManaEffect} on the stack (triggered/loyalty abilities that produce
 * mana, e.g. Koth of the Hammer's {@code -2} or Molten-Core Maestro's Opus trigger), mirroring
 * the mana-ability path in {@code ActivatedAbilityExecutionService}. The mana quantity is a
 * {@link com.github.laxika.magicalvibes.model.amount.DynamicAmount} evaluated against the source
 * permanent (per-permanent counts, charge counters, source power, …); creature-sourced mana is
 * tracked as creature mana.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AwardManaEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AwardManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (AwardManaEffect) effect;
        // Source-relative amounts use the live source permanent when it is still on the
        // battlefield, else the last-known snapshot (e.g. sacrificed as an activation cost).
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

        UUID controllerId = entry.getControllerId();
        ManaPool pool = gameData.playerManaPools.get(controllerId);
        pool.add(e.color(), amount);
        if (source != null && gameQueryService.isCreature(gameData, source)) {
            pool.addCreatureMana(e.color(), amount);
        }

        String playerName = gameData.playerIdToName.get(controllerId);
        String logEntry = playerName + " adds " + amount + " " + e.color().getCode() + ".";
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        log.info("Game {} - {} adds {} {}", gameData.id, playerName, amount, e.color());
    }
}
