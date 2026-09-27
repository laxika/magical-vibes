package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = ((ExileDyingInstantOrSorceryAndMayCastUntilNextTurnEffect) effect)
                .dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (dyingCard == null || ownerId == null
                || (!dyingCard.hasType(CardType.INSTANT) && !dyingCard.hasType(CardType.SORCERY))) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);
        exileSupport.grantPlayUntilOwnersNextTurn(gameData, dyingCardId, entry.getControllerId());

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", dyingCard,
                " and its controller may cast it until the end of their next turn."));
        log.info("Game {} - {} exiles {} and grants a cast permission until next turn",
                gameData.id, entry.getCard().getName(), dyingCard.getName());
    }
}
