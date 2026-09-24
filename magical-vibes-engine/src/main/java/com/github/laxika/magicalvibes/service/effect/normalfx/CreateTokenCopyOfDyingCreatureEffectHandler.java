package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfDyingCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves a token copy from a dying creature's last-known information. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfDyingCreatureEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfDyingCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var dyingCopy = (CreateTokenCopyOfDyingCreatureEffect) effect;
        Card dyingCard = entry.lastKnownPermanentCard(entry.getTriggeringPermanentId());
        if (dyingCard == null) {
            dyingCard = entry.lastKnownPermanentCard(dyingCopy.dyingCardId());
        }
        if (dyingCard == null && dyingCopy.dyingCardId() != null) {
            dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCopy.dyingCardId());
        }
        if (dyingCard == null) {
            return;
        }

        tokenCopySupport.createTokenCopies(gameData, entry, List.of(dyingCard), null,
                entry.getControllerId(), dyingCopy.tokenCopyEffect());
    }
}
