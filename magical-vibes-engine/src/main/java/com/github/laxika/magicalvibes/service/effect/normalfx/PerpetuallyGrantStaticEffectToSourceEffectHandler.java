package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyGrantStaticEffectToSourceEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Adds a static effect to a source card's runtime characteristics. */
@Component
@RequiredArgsConstructor
public class PerpetuallyGrantStaticEffectToSourceEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PerpetuallyGrantStaticEffectToSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var source = entry.getSourcePermanentId() == null
                ? findBattlefieldSource(gameData, entry.getCard())
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            return;
        }

        var copy = source.getCard().createRuntimeCopy();
        copy.addEffect(EffectSlot.STATIC,
                ((PerpetuallyGrantStaticEffectToSourceEffect) effect).staticEffect());
        source.exchangeCard(copy);
    }

    private Permanent findBattlefieldSource(GameData gameData, Card sourceCard) {
        if (sourceCard == null) {
            return null;
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getId().equals(sourceCard.getId())
                        || permanent.getOriginalCard().getId().equals(sourceCard.getId())) {
                    return permanent;
                }
            }
        }
        return null;
    }
}
