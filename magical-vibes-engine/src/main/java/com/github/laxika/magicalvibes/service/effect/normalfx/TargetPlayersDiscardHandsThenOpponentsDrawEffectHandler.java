package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersDiscardHandsThenOpponentsDrawEffect;
import com.github.laxika.magicalvibes.service.DrawService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the targeted hand discard and the resulting draws for Your Plans Mean Nothing. */
@Component
@RequiredArgsConstructor
public class TargetPlayersDiscardHandsThenOpponentsDrawEffectHandler implements NormalEffectHandlerBean {

    private final DiscardHandEffectHandler discardHandEffectHandler;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayersDiscardHandsThenOpponentsDrawEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        TargetPlayersDiscardHandsThenOpponentsDrawEffect discardEffect =
                (TargetPlayersDiscardHandsThenOpponentsDrawEffect) effect;
        List<UUID> targetPlayerIds = entry.targetsForEffect(discardEffect);
        if (targetPlayerIds.isEmpty()) {
            targetPlayerIds = entry.getTargetIds();
        }

        Map<UUID, Integer> discardedCounts = new LinkedHashMap<>();
        for (UUID targetPlayerId : targetPlayerIds) {
            if (!gameData.playerIds.contains(targetPlayerId)) {
                continue;
            }
            int discarded = discardHandEffectHandler.discardHand(
                    gameData, targetPlayerId, entry.getControllerId(), entry.getCard().getName());
            discardedCounts.merge(targetPlayerId, discarded, Integer::sum);
        }

        int controllerDiscarded = discardedCounts.getOrDefault(entry.getControllerId(), 0);
        for (Map.Entry<UUID, Integer> discarded : discardedCounts.entrySet()) {
            UUID playerId = discarded.getKey();
            if (playerId.equals(entry.getControllerId())) {
                continue;
            }
            int drawCount = Math.max(0, discarded.getValue() - 1);
            drawCards(gameData, playerId, drawCount);
        }
        if (controllerDiscarded > 0) {
            drawCards(gameData, entry.getControllerId(), 7);
        }
    }

    private void drawCards(GameData gameData, UUID playerId, int count) {
        for (int i = 0; i < count; i++) {
            drawService.resolveDrawCard(gameData, playerId);
        }
    }
}
