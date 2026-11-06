package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileEquippedCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileEquippedCreatureEffect} (Oathkeeper, Takeno's Daisho): exiles the creature the stack entry's
 * triggering permanent, the Equipment as it last existed on the battlefield (CR 603.10a), was attached to. Does
 * nothing if it was attached to nothing or that creature has already left the battlefield.
 */
@Component
@RequiredArgsConstructor
public class ExileEquippedCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final ExileSupport exileSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileEquippedCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Permanent equipment = entry.getTriggeringPermanentSnapshot();
        if (equipment == null || equipment.getAttachedTo() == null) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, equipment.getAttachedTo());
        if (creature == null) {
            return;
        }
        exileSupport.exilePermanentAndLog(gameData, creature, entry.getCard().getName());
    }
}
