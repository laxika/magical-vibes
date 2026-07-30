package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.ScrambleverseEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ScrambleverseEffect}. Each nonland permanent independently draws a random player;
 * control moves to that player (permanently, via the layer-2 control machinery) and the permanent is
 * untapped whether or not its controller actually changed.
 */
@Component
@RequiredArgsConstructor
public class ScrambleverseEffectHandler implements NormalEffectHandlerBean {

    private static final GainControlOfTargetEffect CONTROL_EFFECT =
            new GainControlOfTargetEffect(ControlDuration.PERMANENT);

    private final CreatureControlService creatureControlService;
    private final TapUntapSupport tapUntapSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ScrambleverseEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<UUID> players = new ArrayList<>(gameData.playerIds);
        if (players.isEmpty()) {
            return;
        }

        // Collect first: applyControlEffect moves permanents between battlefield lists, so we
        // cannot reassign control while iterating.
        List<Permanent> nonlands = new ArrayList<>();
        gameData.forEachPermanent((playerId, permanent) -> {
            if (!permanent.getCard().hasType(CardType.LAND)) {
                nonlands.add(permanent);
            }
        });

        for (Permanent permanent : nonlands) {
            UUID chosen = players.get(ThreadLocalRandom.current().nextInt(players.size()));
            creatureControlService.applyControlEffect(gameData, chosen, permanent, CONTROL_EFFECT,
                    ControlDuration.PERMANENT.toEffectDuration(), null, entry.getCard().getName());
            tapUntapSupport.untapPermanent(gameData, permanent);
        }
    }
}
