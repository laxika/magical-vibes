package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.effect.ExileUncastEnteringCreaturesEffect;

public final class UncastEnteringCreatureExileSupport {

    private UncastEnteringCreatureExileSupport() {
    }

    public static boolean hasActiveStaticReplacement(GameData gameData, Card enteringCard) {
        return gameData.anyPermanentMatches(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(ExileUncastEnteringCreaturesEffect.class::isInstance)
                .map(ExileUncastEnteringCreaturesEffect.class::cast)
                .anyMatch(effect -> !effect.nontokenOnly() || !enteringCard.isToken()));
    }
}
