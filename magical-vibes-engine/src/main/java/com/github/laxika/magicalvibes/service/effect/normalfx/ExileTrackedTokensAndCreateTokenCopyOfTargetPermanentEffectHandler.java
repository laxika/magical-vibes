package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves the Faerie Artisans token replacement trigger. */
@Component
@RequiredArgsConstructor
public class ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final ExileTokensCreatedWithSourceEffectHandler exileTokensHandler;
    private final CreateTokenCopyOfTargetPermanentEffectHandler copyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var composite = (ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect) effect;
        exileTokensHandler.resolve(gameData, entry, new ExileTokensCreatedWithSourceEffect());
        copyHandler.resolve(gameData, entry, composite.copyEffect());
    }
}
