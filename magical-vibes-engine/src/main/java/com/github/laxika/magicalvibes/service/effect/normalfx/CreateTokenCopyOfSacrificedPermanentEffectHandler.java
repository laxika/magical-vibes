package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSacrificedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.action.SacrificePermanentAtControllerEndStepUnlessPays;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;

import java.util.List;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Ashling-style copies of a sacrificed permanent using its last-known card. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSacrificedPermanentEffectHandler implements NormalEffectHandlerBean {

    private static final String TOKEN_SACRIFICE_COST = "{W}{U}{B}{R}{G}";

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSacrificedPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        CreateTokenCopyOfSacrificedPermanentEffect copyEffect =
                (CreateTokenCopyOfSacrificedPermanentEffect) effect;
        if (copyEffect.copiedCard() == null) {
            return;
        }

        List<UUID> tokenIds = tokenCopySupport.createTokenCopies(
                gameData, entry, List.of(copyEffect.copiedCard()), null,
                new CreateTokenCopyOfTargetPermanentEffect());
        for (UUID tokenId : tokenIds) {
            Permanent token = gameQueryService.findPermanentById(gameData, tokenId);
            if (token == null) {
                continue;
            }
            token.getGrantedKeywords().add(Keyword.HASTE);
            gameData.queueDelayedAction(new SacrificePermanentAtControllerEndStepUnlessPays(
                    tokenId, entry.getControllerId(), entry.getCard(), TOKEN_SACRIFICE_COST));
        }
    }
}
