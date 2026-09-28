package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfRandomCardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfRandomCardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfRandomCardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var randomEffect = (CreateTokenCopyOfRandomCardEffect) effect;
        Card sourceCard = randomEffect.cardFactories()
                .get(ThreadLocalRandom.current().nextInt(randomEffect.cardFactories().size()))
                .get();
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        tokenCopySupport.createTokenCopies(
                gameData, entry, List.of(sourceCard), sourcePermanent,
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
