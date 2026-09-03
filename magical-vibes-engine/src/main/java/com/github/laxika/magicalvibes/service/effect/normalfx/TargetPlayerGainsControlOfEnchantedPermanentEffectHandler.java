package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerGainsControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerGainsControlOfEnchantedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerGainsControlOfEnchantedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
            return;
        }

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }

        Permanent enchantedPermanent = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchantedPermanent == null) {
            return;
        }

        creatureControlService.applyControlEffect(
                gameData,
                entry.getTargetId(),
                enchantedPermanent,
                new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                EffectDuration.PERMANENT,
                null,
                entry.getCard().getName());
    }
}
