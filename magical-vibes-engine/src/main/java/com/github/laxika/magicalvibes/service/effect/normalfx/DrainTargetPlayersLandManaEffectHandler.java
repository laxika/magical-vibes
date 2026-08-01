package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrainTargetPlayersLandManaEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.UUID;

/**
 * Resolves {@link DrainTargetPlayersLandManaEffect} (Drain Power): each untapped land the target
 * player controls is tapped for the mana it produces (added to that player's pool), then the
 * target player's entire pool is emptied and the spell's controller adds an equal amount of mana.
 *
 * <p>Lands whose mana ability requires a color choice (any-color producers) contribute colorless,
 * and dual/multi-ability lands use their first tap-for-mana ability; the common case of fixed
 * single-color lands is exact.</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DrainTargetPlayersLandManaEffectHandler implements NormalEffectHandlerBean {

    private final LandManaDrainSupport landManaDrainSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrainTargetPlayersLandManaEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId)) {
            return;
        }
        ManaPool targetPool = gameData.playerManaPools.get(targetPlayerId);
        ManaPool controllerPool = gameData.playerManaPools.get(entry.getControllerId());
        if (targetPool == null || controllerPool == null) {
            return;
        }

        landManaDrainSupport.activateManaAbilityOfEachLand(gameData, targetPlayerId);

        // The target player loses all unspent mana; the controller adds the mana lost this way.
        Map<String, Integer> lostByCode = targetPool.toMap();
        int totalTransferred = 0;
        for (ManaColor color : ManaColor.values()) {
            int amount = lostByCode.getOrDefault(color.getCode(), 0);
            if (amount > 0) {
                controllerPool.add(color, amount);
                totalTransferred += amount;
            }
        }
        targetPool.clear();
        targetPool.clearPersistentMana();

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" drains " + totalTransferred + " mana.").build());
        log.info("Game {} - {} drains {} mana from target player", gameData.id, entry.getCard().getName(), totalTransferred);
    }
}
