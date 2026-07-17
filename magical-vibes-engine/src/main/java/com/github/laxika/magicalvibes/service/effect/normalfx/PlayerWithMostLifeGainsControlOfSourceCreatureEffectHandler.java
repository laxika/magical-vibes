package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PlayerWithMostLifeGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PlayerWithMostLifeGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PlayerWithMostLifeGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent source = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) {
                continue;
            }
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(entry.getCard().getId())) {
                    source = permanent;
                    break;
                }
            }
            if (source != null) {
                break;
            }
        }

        if (source == null) {
            return;
        }

        // Intervening-if: a single player must have strictly more life than each other player.
        int highestLife = gameData.orderedPlayerIds.stream()
                .mapToInt(gameData::getLife)
                .max()
                .orElse(0);
        List<UUID> leaders = gameData.orderedPlayerIds.stream()
                .filter(playerId -> gameData.getLife(playerId) == highestLife)
                .toList();
        if (leaders.size() != 1) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, leaders.get(0), source,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
