package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfAuraAttachedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the non-targeting control effect created by an Aura-attachment trigger. */
@Component
@RequiredArgsConstructor
public class GainControlOfAuraAttachedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfAuraAttachedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Permanent enchanted = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (aura == null || enchanted == null || !enchanted.getId().equals(aura.getAttachedTo())) {
            return;
        }

        creatureControlService.applyControlEffect(gameData, entry.getControllerId(), enchanted,
                effect, EffectDuration.WHILE_ATTACHED, aura.getId(), entry.getCard().getName());
    }
}
