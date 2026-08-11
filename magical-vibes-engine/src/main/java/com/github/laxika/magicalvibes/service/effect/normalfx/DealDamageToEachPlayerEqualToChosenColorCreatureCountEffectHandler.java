package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachPlayerEqualToChosenColorCreatureCountEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the color choice and per-player creature count for Searing Rays. */
@Component
@RequiredArgsConstructor
public class DealDamageToEachPlayerEqualToChosenColorCreatureCountEffectHandler
        implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachPlayerEqualToChosenColorCreatureCountEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (gameData.chosenSpellColor == null) {
            gameData.rerunCurrentEffectAfterInteraction = true;
            playerInputService.beginSpellColorChoice(gameData, entry.getControllerId());
            return;
        }

        CardColor chosenColor = gameData.chosenSpellColor;
        gameData.chosenSpellColor = null;
        gameData.rerunCurrentEffectAfterInteraction = false;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            return;
        }

        for (UUID playerId : gameData.orderedPlayerIds) {
            int count = countCreaturesOfColor(gameData, playerId, chosenColor);
            if (count == 0) {
                continue;
            }
            int damage = gameQueryService.applyDamageMultiplier(gameData, count, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }

    private int countCreaturesOfColor(GameData gameData, UUID playerId, CardColor color) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        int count = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.isCreature(gameData, permanent)
                    && gameQueryService.getEffectiveColors(gameData, permanent).contains(color)) {
                count++;
            }
        }
        return count;
    }
}
