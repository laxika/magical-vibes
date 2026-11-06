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
 * Resolves {@link SetBasePowerToughnessOfCreaturesThatDamagedSourceEffect}: finds the creatures that dealt damage to
 * the source this turn, then delegates each individual base-stat change to the normal base power/toughness
 * handler.
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
        Set<UUID> damagerIds = damagerIdsOf(gameData, entry);
        for (UUID playerId : apnapOrder(gameData)) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (!damagerIds.contains(permanent.getId()) || !gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                StackEntry targetEntry = new StackEntry(entry);
                targetEntry.setTargetId(permanent.getId());
                setBasePowerToughnessEffectHandler.resolve(gameData, targetEntry,
                        SetBasePowerToughnessEffect.indefinitely(e.power(), e.toughness()));
            }
        }
    }

    /**
     * The permanents that dealt damage to the source this turn: for a death trigger, to the stack entry's triggering
     * permanent, the source as it last existed on the battlefield (CR 603.10a); otherwise to the source itself.
     */
    private Set<UUID> damagerIdsOf(GameData gameData, StackEntry entry) {
        Permanent source = entry.getTriggeringPermanentSnapshot();
        if (source == null && entry.getSourcePermanentId() != null) {
            source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        return source == null ? Set.of() : Set.copyOf(source.getPermanentIdsThatDealtDamageThisTurn());
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
