package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.ApproachOfTheSecondSunEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApproachOfTheSecondSunEffectHandler implements NormalEffectHandlerBean {

    /** "Seventh from the top" as a 0-based library index. */
    private static final int SEVENTH_FROM_TOP = 6;

    private final WinGameEffectHandler winGameEffectHandler;
    private final LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ApproachOfTheSecondSunEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        boolean castFromHand = entry.getSourceZone() == Zone.HAND;
        // This cast is already recorded, so a count >= 2 means at least one *other* same-named spell
        // was cast earlier this game.
        int sameNameCasts = gameData.getSpellsCastThisGameByNameCount(controllerId, entry.getCard().getName());

        if (castFromHand && sameNameCasts >= 2) {
            winGameEffectHandler.resolve(gameData, entry, new WinGameEffect());
            return;
        }

        // Otherwise: put the spell into its owner's library seventh from the top and gain 7 life.
        entry.setPutIntoLibraryPositionAfterResolving(SEVENTH_FROM_TOP);
        lifeSupport.applyGainLife(gameData, controllerId, 7, null, entry.getCard(), entry.getEntryType());
    }
}
