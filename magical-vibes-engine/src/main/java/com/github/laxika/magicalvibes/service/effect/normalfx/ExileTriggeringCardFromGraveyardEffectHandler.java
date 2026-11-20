package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.StepOutcome;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves "exile it" of an ability about a card put into a graveyard (a creature-death trigger such as Patron of the
 * Vein's or Illicit Masquerade's, Planar Void, a copy's reflexive ability): exiles the stack entry's triggering card
 * from its owner's graveyard. A card that left that graveyard and came back is a new object the ability no longer
 * refers to (CR 400.7), so it stays. Records the {@link StepKind#EXILE} step (the card and its owner, or nothing).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.recordStep(StepKind.EXILE, StepOutcome.NONE);
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        Card triggeringCard = gameQueryService.findCardInGraveyardById(gameData, triggeringCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, triggeringCardId);
        if (triggeringCard == null || ownerId == null
                || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(triggeringCardId))) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, triggeringCardId);
        exileService.exileCard(gameData, ownerId, triggeringCard);
        entry.recordStep(StepKind.EXILE, StepOutcome.ofObjects(List.of(triggeringCardId), List.of(ownerId)));
        gameLogService.append(gameData, GameLog.cardTextCard(triggeringCard, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} from a graveyard", gameData.id,
                entry.getCard().getName(), triggeringCard.getName());
    }
}
