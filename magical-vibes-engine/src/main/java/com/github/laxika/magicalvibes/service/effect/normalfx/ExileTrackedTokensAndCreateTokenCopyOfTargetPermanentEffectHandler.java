package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTokensCreatedWithSourceEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

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

    /**
     * Creates the new copy first, then exiles only the tokens that were tracked before it was created, so the
     * new copy is never exiled with them.
     */
    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var composite = (ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect) effect;
        UUID sourceId = entry.getSourcePermanentId();
        if (sourceId == null) {
            copyHandler.resolve(gameData, entry, composite.copyEffect());
            return;
        }
        Set<UUID> previousTokens = gameData.sourceCreatedTokens.remove(sourceId);
        copyHandler.resolve(gameData, entry, composite.copyEffect());
        Set<UUID> newTokens = gameData.sourceCreatedTokens.remove(sourceId);
        if (previousTokens != null) {
            gameData.sourceCreatedTokens.put(sourceId, previousTokens);
            exileTokensHandler.resolve(gameData, entry, new ExileTokensCreatedWithSourceEffect());
        }
        if (newTokens != null) {
            gameData.sourceCreatedTokens.put(sourceId, newTokens);
        }
    }
}
