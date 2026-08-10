package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the controller of a triggering spell or ability gaining control of the source Aura's
 * enchanted permanent.
 */
@Component
@RequiredArgsConstructor
public class TriggeringObjectControllerGainsControlOfEnchantedPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final CreatureControlService creatureControlService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TriggeringObjectControllerGainsControlOfEnchantedPermanentEffect) effect;
        StackEntry triggeringEntry = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        Permanent aura = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (triggeringEntry == null || aura == null || aura.getAttachedTo() == null) return;

        Permanent enchantedPermanent = gameQueryService.findPermanentById(gameData, aura.getAttachedTo());
        UUID newControllerId = triggeringEntry.getControllerId();
        if (enchantedPermanent == null || newControllerId == null) return;

        creatureControlService.applyControlEffect(gameData, newControllerId, enchantedPermanent,
                new GainControlOfTargetEffect(e.duration()),
                e.duration().toEffectDuration(), null, entry.getCard().getName());
    }
}
