package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEnteringTokenForTargetPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Lucy MacLean's token-copy trigger. */
@Component
@RequiredArgsConstructor
public class CreateTokenCopyOfEnteringTokenForTargetPlayerEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopyOfEnteringTokenForTargetPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetPlayerId = entry.getTargetId();
        UUID enteringTokenId = entry.getTriggeringPermanentId();
        if (targetPlayerId == null || !gameData.playerIds.contains(targetPlayerId) || enteringTokenId == null) {
            return;
        }

        // The token may have left the battlefield since the ability triggered; its copy is then made from
        // last known information (CR 608.2h).
        Permanent enteringToken = gameQueryService.findPermanentById(gameData, enteringTokenId);
        Card enteringCard = enteringToken != null
                ? enteringToken.getCard() : entry.lastKnownPermanentCard(enteringTokenId);
        if (enteringCard == null) {
            return;
        }

        UUID enteringTokenControllerId = enteringToken != null
                ? gameQueryService.findPermanentController(gameData, enteringTokenId)
                : entry.getTriggeringPermanentControllerId();
        if (enteringTokenControllerId == null || enteringTokenControllerId.equals(targetPlayerId)) {
            return;
        }

        List<UUID> createdTokenIds = tokenCopySupport.createTokenCopiesWithoutReplacementOrder(
                gameData,
                entry,
                List.of(enteringCard),
                enteringToken,
                targetPlayerId,
                new CreateTokenCopyOfTargetPermanentEffect());
        if (!createdTokenIds.isEmpty() && !targetPlayerId.equals(entry.getControllerId())) {
            playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), 1);
        }
    }
}
