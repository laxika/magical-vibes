package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PhaseInLinkedPermanentEffect;
import com.github.laxika.magicalvibes.service.turn.PhasingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a trigger that phases in the permanent linked to its source. */
@Component
@RequiredArgsConstructor
public class PhaseInLinkedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final PhasingService phasingService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PhaseInLinkedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() != null) {
            phasingService.phaseInLinkedPermanents(gameData, entry.getSourcePermanentId());
        }
    }
}
