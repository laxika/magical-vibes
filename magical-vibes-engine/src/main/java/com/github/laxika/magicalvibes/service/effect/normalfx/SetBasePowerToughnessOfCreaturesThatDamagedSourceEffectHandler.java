package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves the historical damage lookup for
 * {@link SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect}, then delegates each individual
 * base-stat change to the normal base power/toughness handler.
 */
@Component
@RequiredArgsConstructor
public class SetBasePowerToughnessOfCreaturesThatDamagedSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SetBasePowerToughnessEffectHandler setBasePowerToughnessEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect) effect;
        UUID sourceCardId = entry.getCard().getId();
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : List.copyOf(battlefield)) {
                Set<UUID> damaged = gameData.creatureCardsDamagedBySourceThatDiedThisTurn
                        .get(permanent.getId());
                if (damaged == null || !damaged.contains(sourceCardId)
                        || !gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                StackEntry targetEntry = new StackEntry(entry);
                targetEntry.setTargetId(permanent.getId());
                setBasePowerToughnessEffectHandler.resolve(gameData, targetEntry,
                        SetBasePowerToughnessEffect.indefinitely(e.power(), e.toughness()));
            }
        }
    }

    private static List<UUID> apnapOrder(GameData gameData) {
        List<UUID> ordered = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = ordered.indexOf(gameData.activePlayerId);
        if (activeIndex <= 0) {
            return ordered;
        }
        List<UUID> rotated = new ArrayList<>(ordered.subList(activeIndex, ordered.size()));
        rotated.addAll(ordered.subList(0, activeIndex));
        return rotated;
    }
}
