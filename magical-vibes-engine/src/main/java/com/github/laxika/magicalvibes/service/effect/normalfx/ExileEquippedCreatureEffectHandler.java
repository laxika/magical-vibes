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
 * Resolves {@link ExileEquippedCreatureEffect} (Oathkeeper, Takeno's Daisho): exiles the creature
 * the source Equipment was attached to when it was put into a graveyard. Fizzles if that creature
 * has already left the battlefield.
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
        var e = (ExileEquippedCreatureEffect) effect;
        if (e.equippedCreatureId() == null) {
            return;
        }
        Permanent creature = gameQueryService.findPermanentById(gameData, e.equippedCreatureId());
        if (creature == null) {
            return;
        }
        exileSupport.exilePermanentAndLog(gameData, creature, entry.getCard().getName());
    }
}
