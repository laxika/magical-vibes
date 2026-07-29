package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseOutSelfAndCombatOpponentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Resolves {@link PhaseOutSelfAndCombatOpponentEffect}: phases out the source permanent and the
 * combat opponent carried as the stack entry's non-targeting target. Either one that already left
 * the battlefield is simply skipped; the other still phases out.
 */
@Component
@RequiredArgsConstructor
public class PhaseOutSelfAndCombatOpponentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseOutSelfAndCombatOpponentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Set<Permanent> phasingOut = new LinkedHashSet<>();
        addIfPresent(gameData, phasingOut, entry.getSourcePermanentId());
        addIfPresent(gameData, phasingOut, entry.getTargetId());
        if (phasingOut.isEmpty()) {
            return;
        }

        phasingService.phaseOut(gameData, phasingOut);
    }

    private void addIfPresent(GameData gameData, Set<Permanent> phasingOut, UUID permanentId) {
        if (permanentId == null) {
            return;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent != null) {
            phasingOut.add(permanent);
        }
    }
}
