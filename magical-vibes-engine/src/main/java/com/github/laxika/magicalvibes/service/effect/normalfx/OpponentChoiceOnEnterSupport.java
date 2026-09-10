package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ChooseOpponentOnEnterEffect;

import java.util.UUID;

/** Applies an as-enters opponent choice before characteristic-defining abilities count that player. */
public final class OpponentChoiceOnEnterSupport {

    private OpponentChoiceOnEnterSupport() {
    }

    /** In two-player games the entering permanent's controller has exactly one opponent to choose. */
    public static void applyIfPresent(GameData gameData, UUID controllerId, Permanent permanent) {
        if (!permanent.isFaceDown() && permanent.getCard().getEffects(EffectSlot.ON_ENTER_BATTLEFIELD)
                .stream().anyMatch(ChooseOpponentOnEnterEffect.class::isInstance)) {
            permanent.setRememberedTargetPlayerId(gameData.playerIds.stream()
                    .filter(playerId -> !playerId.equals(controllerId)).findFirst().orElse(null));
        }
    }
}
