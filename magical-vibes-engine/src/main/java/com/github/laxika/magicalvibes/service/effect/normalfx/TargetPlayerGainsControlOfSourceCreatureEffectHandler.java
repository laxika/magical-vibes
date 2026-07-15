package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfSourceCreatureEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfSourceCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfSourceCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        
                if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
                    return;
                }

                UUID newControllerId = entry.getTargetId();
                Permanent source = null;
                for (UUID playerId : gameData.orderedPlayerIds) {
                    List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
                    if (battlefield == null) continue;
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
                    String fizzleLog = entry.getCard().getName() + "'s ability has no effect (it is no longer on the battlefield).";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(fizzleLog));
                    return;
                }

                creatureControlService.applyControlEffect(gameData, newControllerId, source,
                        new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                        EffectDuration.PERMANENT, null, entry.getCard().getName());
    }
}
