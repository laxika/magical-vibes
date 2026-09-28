package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesTargetPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves targeted mass goad by snapshotting the target player's creatures. */
@Component
@RequiredArgsConstructor
public class GoadCreaturesTargetPlayerControlsUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreaturesTargetPlayerControlsUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> targetPlayerIds = entry.targetsForEffect(effect);
        if (targetPlayerIds.isEmpty() && entry.getTargetId() != null) {
            targetPlayerIds = List.of(entry.getTargetId());
        }

        for (UUID targetPlayerId : targetPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(targetPlayerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (!gameQueryService.isCreature(gameData, permanent)) {
                    continue;
                }
                GoadCreaturesUntilNextTurnSnapshotEffect goad =
                        new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentTruePredicate());
                gameData.addFloatingEffect(new FloatingContinuousEffect(
                        UUID.randomUUID(), entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                        entry.getSourcePermanentId(), entry.getControllerId(), goad, permanent.getId(),
                        null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
            }
        }
    }
}
