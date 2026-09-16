package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSourceForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID tokenControllerId = entry.getTargetId();
        if (tokenControllerId == null || !gameData.playerIds.contains(tokenControllerId)) {
            return;
        }

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card sourceCard = sourcePermanent != null ? sourcePermanent.getCard() : entry.getCard();
        if (sourceCard == null) {
            return;
        }

        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                List.of(sourceCard),
                sourcePermanent,
                tokenControllerId,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
