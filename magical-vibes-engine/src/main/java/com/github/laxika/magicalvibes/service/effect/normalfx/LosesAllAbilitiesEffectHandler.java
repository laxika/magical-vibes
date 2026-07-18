package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class LosesAllAbilitiesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return LosesAllAbilitiesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (LosesAllAbilitiesEffect) effect;
        UUID targetId = switch (e.scope()) {
            case SELF -> entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getTargetId();
            case TARGET -> entry.getTargetId();
            default -> null;
        };
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        // CR 613 layer engine: a one-shot "loses all abilities until end of turn" (Merfolk
        // Trickster) is a floating layer-6 effect with its own timestamp — a later-timestamp
        // keyword grant (Wings of Velis Vel) survives it. The legacy flag is still set for
        // direct Permanent.hasKeyword/flag readers; the layered pass treats the flag as a
        // seed-time removal and then replays this effect at its real timestamp.
        target.setLosesAllAbilitiesUntilEndOfTurn(true);
        gameData.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(),
                entry.getCard().getName(), null, entry.getControllerId(), e,
                target.getId(), null, null, EffectDuration.UNTIL_END_OF_TURN, 0));

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(target.getCard(), " loses all abilities until end of turn."));
        log.info("Game {} - {} loses all abilities until end of turn", gameData.id, target.getCard().getName());
    }
}
