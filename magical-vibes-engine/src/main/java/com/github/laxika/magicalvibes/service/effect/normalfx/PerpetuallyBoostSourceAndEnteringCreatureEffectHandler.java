package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyBoostSourceAndEnteringCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves a perpetual boost for an enter-trigger source and the creature that triggered it. */
@Component
@RequiredArgsConstructor
public class PerpetuallyBoostSourceAndEnteringCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyBoostSourceAndEnteringCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var boost = (PerpetuallyBoostSourceAndEnteringCreatureEffect) effect;
        applyBoost(gameData, entry.getSourcePermanentId(), boost);
        applyBoost(gameData, entry.getTriggeringPermanentId(), boost);
    }

    private void applyBoost(GameData gameData, java.util.UUID permanentId,
                            PerpetuallyBoostSourceAndEnteringCreatureEffect boost) {
        if (permanentId == null) {
            return;
        }
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        if (permanent == null) {
            return;
        }

        Card modifiedCard = permanent.getCard().createRuntimeCopy();
        modifiedCard.addEffect(EffectSlot.STATIC,
                new StaticBoostEffect(boost.powerBoost(), boost.toughnessBoost(), GrantScope.SELF));
        modifiedCard.freeze();
        permanent.exchangeCard(modifiedCard);
    }
}
