package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.effect.TriggeringSpellControllerSacrificesPermanentsEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a sacrifice directed at the controller of the spell or ability that caused a trigger. */
@Component
@RequiredArgsConstructor
public class TriggeringSpellControllerSacrificesPermanentsEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final SacrificePermanentsEffectHandler sacrificePermanentsEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TriggeringSpellControllerSacrificesPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (TriggeringSpellControllerSacrificesPermanentsEffect) effect;
        StackEntry triggeringEntry = gameQueryService.findStackEntryByCardId(gameData, entry.getTargetId());
        if (triggeringEntry == null || triggeringEntry.getControllerId() == null) {
            return;
        }

        sacrificePermanentsEffectHandler.resolveForPlayer(
                gameData,
                entry,
                new SacrificePermanentsEffect(e.count(), e.filter(), SacrificeRecipient.CONTROLLER),
                triggeringEntry.getControllerId());
    }
}
