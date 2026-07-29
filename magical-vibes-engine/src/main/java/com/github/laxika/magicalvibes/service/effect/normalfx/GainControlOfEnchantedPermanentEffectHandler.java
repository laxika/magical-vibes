package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link GainControlOfEnchantedPermanentEffect} by re-deriving the enchanted permanent
 * from the source Aura's {@code attachedTo} and creating a floating layer-2 control effect
 * (CR 613.2/613.7) for the requested duration. Fizzles silently when the Aura has left the
 * battlefield or is no longer attached.
 */
@Component
@RequiredArgsConstructor
public class GainControlOfEnchantedPermanentEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final CreatureControlService creatureControlService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return GainControlOfEnchantedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (GainControlOfEnchantedPermanentEffect) effect;

        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (aura == null || aura.getAttachedTo() == null) {
            return;
        }

        Permanent enchanted = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        if (enchanted == null) {
            return;
        }

        UUID sourcePermanentId = e.duration().isSourceLinked() ? aura.getId() : null;
        creatureControlService.applyControlEffect(gameData, entry.getControllerId(), enchanted,
                e, e.duration().toEffectDuration(), sourcePermanentId, entry.getCard().getName());
    }
}
