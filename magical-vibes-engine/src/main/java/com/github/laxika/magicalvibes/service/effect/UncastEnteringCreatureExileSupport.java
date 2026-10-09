package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.ExileSelfIfUncastOrUnpaidEffect;
import com.github.laxika.magicalvibes.model.effect.ExileUncastEnteringCreaturesEffect;

public final class UncastEnteringCreatureExileSupport {

    private UncastEnteringCreatureExileSupport() {
    }

    public static boolean hasActiveStaticReplacement(GameData gameData, Card enteringCard,
                                                    com.github.laxika.magicalvibes.service.battlefield.GameQueryService query) {
        return gameData.anyPermanentMatches(source -> !source.getCard().getId().equals(enteringCard.getId())
                && !query.hasLostAllAbilities(gameData, source)
                && source.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(ExileUncastEnteringCreaturesEffect.class::isInstance)
                .map(ExileUncastEnteringCreaturesEffect.class::cast)
                .anyMatch(effect -> !effect.nontokenOnly() || !enteringCard.isToken()));
    }

    public static boolean hasSelfEntryReplacement(Permanent enteringPermanent) {
        boolean marked = enteringPermanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(ExileSelfIfUncastOrUnpaidEffect.class::isInstance);
        return marked && (!enteringPermanent.isCast() || enteringPermanent.getManaSpentToCast() == 0);
    }
}
