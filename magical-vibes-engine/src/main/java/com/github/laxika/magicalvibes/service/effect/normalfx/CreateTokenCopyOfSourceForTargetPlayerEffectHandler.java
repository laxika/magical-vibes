package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.battlefield.PermanentCopierService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves "target player creates a token that's a copy of it" (Wedding Ring). That player creates the token, so their
 * "the first time you would create one or more tokens" replacements (Mirrormind Crown, Moonlit Meditation) are offered
 * to them first (CR 111.2).
 */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfSourceForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final CreateTokenCopyOfTargetPermanentEffectHandler tokenCopyHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfSourceForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getTargetId() == null || !gameData.playerIds.contains(entry.getTargetId())) {
            return;
        }

        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? entry.getSourcePermanentSnapshot()
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        Card sourceCard = sourcePermanent != null ? PermanentCopierService.cardToCopy(sourcePermanent) : entry.getCard();
        if (sourceCard == null) {
            return;
        }
        if (tokenCopyHandler.replaceFirstTokenCreation(gameData, entry, 1, entry.getTargetId())) {
            return;
        }

        tokenCopySupport.createTokenCopies(
                gameData,
                entry,
                List.of(sourceCard),
                sourcePermanent,
                entry.getTargetId(),
                new CreateTokenCopyOfTargetPermanentEffect());
    }
}
