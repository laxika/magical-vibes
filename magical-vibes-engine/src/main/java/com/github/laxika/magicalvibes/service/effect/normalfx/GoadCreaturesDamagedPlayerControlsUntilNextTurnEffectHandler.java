package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves a combat-damage trigger by goading the damaged player's creatures. */
@Component
@RequiredArgsConstructor
public class GoadCreaturesDamagedPlayerControlsUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GoadCreaturesDamagedPlayerControlsUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID damagedPlayerId = entry.getTargetId();
        if (damagedPlayerId == null) {
            return;
        }

        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of())) {
            if (!gameQueryService.isCreature(gameData, permanent)) {
                continue;
            }
            gameData.addFloatingEffect(new FloatingContinuousEffect(
                    UUID.randomUUID(), entry.getCard() == null ? "Goad" : entry.getCard().getName(),
                    entry.getSourcePermanentId(), entry.getControllerId(),
                    new GoadCreaturesUntilNextTurnSnapshotEffect(new PermanentTruePredicate()),
                    permanent.getId(), null, null, EffectDuration.UNTIL_YOUR_NEXT_TURN, 0));
        }
    }
}
