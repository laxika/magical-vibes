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
 * <p>The affected player makes any required color choices. Dual/multi-ability lands use their
 * first available tap-for-mana ability, including abilities with payable mana costs.</p>
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

        landManaDrainSupport.activateManaAbilityOfEachLand(
                gameData, targetPlayerId, entry.getControllerId());

        // The target player loses all unspent mana; the controller adds the mana lost this way.
        if (targetPlayerId.equals(entry.getControllerId())) {
            gameLogService.append(gameData, GameLog.builder().card(entry.getCard())
                    .text(" drains " + targetPool.getTotalAllMana() + " mana.").build());
            return;
        }

        int totalTransferred = targetPool.getTotalAllMana();
        if (controllerPool.getTotalAllMana() == 0) {
            gameData.playerManaPools.put(entry.getControllerId(), new ManaPool(targetPool));
        } else {
            Map<String, Integer> lostByCode = targetPool.toMap();
            for (ManaColor color : ManaColor.values()) {
                int regular = targetPool.get(color);
                if (regular > 0) {
                    controllerPool.add(color, regular);
                }
                int abilityOnly = targetPool.getAbilityOnlyMana(color);
                if (abilityOnly > 0) {
                    controllerPool.addAbilityOnlyMana(color, abilityOnly);
                }
                int unclassifiedRestricted = lostByCode.getOrDefault(color.getCode(), 0)
                        - regular - abilityOnly;
                if (unclassifiedRestricted > 0) {
                    controllerPool.add(color, unclassifiedRestricted);
                }
            }
        }
        targetPool.clear();
        targetPool.clearPersistentMana();

        gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text(" drains " + totalTransferred + " mana.").build());
        log.info("Game {} - {} drains {} mana from target player", gameData.id, entry.getCard().getName(), totalTransferred);
    }
}
