package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToOpponentsAndPlaneswalkersDamagedBySourceEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) return;

        Set<UUID> recipients = gameData.damageRecipientsBySource.getOrDefault(sourceId, Set.of());
        int rawDamage = gameQueryService.applyDamageMultiplier(gameData, e.damage(), entry);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(entry.getControllerId()) && recipients.contains(playerId)) {
                damageSupport.resolveAnyTargetDamage(gameData, entry, playerId, rawDamage, false);
            }
        }

        gameData.playerBattlefields.values().forEach(battlefield -> {
            for (Permanent permanent : new ArrayList<>(battlefield)) {
                if (recipients.contains(permanent.getId())
                        && gameQueryService.isPlaneswalker(gameData, permanent)) {
                    damageSupport.resolveAnyTargetDamage(gameData, entry, permanent.getId(), rawDamage, false);
                }
            }
        });

        gameOutcomeService.checkWinCondition(gameData);
    }
}
