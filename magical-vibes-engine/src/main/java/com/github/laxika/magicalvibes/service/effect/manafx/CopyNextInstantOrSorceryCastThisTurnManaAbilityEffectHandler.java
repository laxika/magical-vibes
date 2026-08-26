package com.github.laxika.magicalvibes.service.effect.manafx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CopyNextInstantOrSorceryCastThisTurnEffect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.UUID;

@Slf4j
@Component
public class CopyNextInstantOrSorceryCastThisTurnManaAbilityEffectHandler implements ManaAbilityEffectHandler {

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CopyNextInstantOrSorceryCastThisTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        int manaMultiplier, boolean creatureSource) {
        gameData.pendingNextInstantSorceryCopyThisTurnCount.merge(playerId, 1, Integer::sum);
        log.info("Game {} - {} will copy their next instant or sorcery spell this turn",
                gameData.id, playerId);
    }

    @Override
    public void resolve(GameData gameData, UUID playerId, Player player, Permanent permanent,
                        int manaMultiplier, boolean creatureSource, CardEffect effect) {
        CopyNextInstantOrSorceryCastThisTurnEffect copyEffect =
                (CopyNextInstantOrSorceryCastThisTurnEffect) effect;
        if (copyEffect.maxManaValue() == null) {
            resolve(gameData, playerId, player, permanent, manaMultiplier, creatureSource);
            return;
        }
        gameData.pendingNextInstantSorceryCopyThisTurnMaxManaValues
                .computeIfAbsent(playerId, ignored -> new ArrayList<>())
                .add(copyEffect.maxManaValue());
        log.info("Game {} - {} will copy their next instant or sorcery spell with mana value at most {} this turn",
                gameData.id, playerId, copyEffect.maxManaValue());
    }
}
