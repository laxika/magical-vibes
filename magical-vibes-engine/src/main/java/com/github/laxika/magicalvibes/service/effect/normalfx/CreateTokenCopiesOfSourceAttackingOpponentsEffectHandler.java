package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopiesOfSourceAttackingOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class CreateTokenCopiesOfSourceAttackingOpponentsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final TokenCopySupport tokenCopySupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return CreateTokenCopiesOfSourceAttackingOpponentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        Card sourceCard = entry.getCard();
        if (sourceCard == null || !sourceCard.hasType(CardType.CREATURE)) {
            return;
        }

        int tokenCount = gameQueryService.getTokenCreationAmount(
                gameData,
                entry.getControllerId(),
                1,
                sourceCard.getSubtypes() == null ? List.of() : sourceCard.getSubtypes(),
                true);
        if (tokenCount <= 0) {
            return;
        }

        CreateTokenCopyOfTargetPermanentEffect copyEffect =
                new CreateTokenCopyOfTargetPermanentEffect(true, false, true, true);
        for (UUID opponentId : gameData.orderedPlayerIds) {
            if (!gameData.playerIds.contains(opponentId) || opponentId.equals(entry.getControllerId())) {
                continue;
            }
            tokenCopySupport.createTokenCopies(
                    gameData,
                    entry,
                    List.of(sourceCard),
                    null,
                    entry.getControllerId(),
                    copyEffect,
                    Collections.nCopies(tokenCount, opponentId));
        }
    }
}
