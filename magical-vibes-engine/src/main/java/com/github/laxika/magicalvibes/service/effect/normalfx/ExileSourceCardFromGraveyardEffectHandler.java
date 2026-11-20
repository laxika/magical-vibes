package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LastKnownCardStats;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.List;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link ExileSourceCardFromGraveyardEffect}: exiles the source's card from its graveyard, if it is still the
 * card the event named. Records the {@link StepKind#EXILE} step (the card and its owner, or nothing) for "if you do".
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        UUID cardId = entry.getCard().getId();
        long expectedEntryVersion = entry.sourceCardGraveyardEntryVersion();
        if (expectedEntryVersion >= 0
                && gameData.graveyardEntryVersion(cardId) != expectedEntryVersion) {
            return;
        }
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} exile-on-death trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (((ExileSourceCardFromGraveyardEffect) effect).onlyFromControllersGraveyard()
                && !entry.getControllerId().equals(ownerId)) {
            return;
        }
        List<LastKnownCardStats> known = gameQueryService.asInGraveyard(gameData, List.of(sourceCard));
        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
        if (ownerId != null) {
            exileService.exileCard(gameData, ownerId, sourceCard);
            entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(cardId), List.of(ownerId))
                    .withLastKnownCards(known));
        }

        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " is exiled."));
        log.info("Game {} - {} exiled from graveyard on death", gameData.id, sourceCard.getName());
    }
}
