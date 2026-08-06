package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextUpkeepWithHasteEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * Resolves {@link ExileSelfReturnAtNextUpkeepWithHasteEffect} by exiling the source permanent and
 * queueing a {@link PendingExileReturn} that brings it back under its owner's control at the
 * beginning of that owner's next upkeep, with haste. No-op when the source already left the
 * battlefield.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSelfReturnAtNextUpkeepWithHasteEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfReturnAtNextUpkeepWithHasteEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        if (entry.getSourcePermanentId() == null) {
            return;
        }

        Permanent self = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (self == null) {
            return;
        }

        List<Card> cards = self.cardsLeavingBattlefield();
        Card card = cards.getFirst();
        permanentRemovalService.removePermanentToExile(gameData, self);

        // "under its owner's control" — the ability's controller is the fallback when ownership was
        // never stamped on the card.
        UUID returnControllerId = card.getOwnerId() != null ? card.getOwnerId() : entry.getControllerId();

        gameData.queueDelayedAction(new PendingExileReturn(
                card, returnControllerId, false, false, TurnStep.UPKEEP, 0,
                cards.size() == 1 ? List.of() : cards.subList(1, cards.size()), true, true));

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled. It returns at the beginning of its owner's next upkeep with haste."));
        log.info("Game {} - {} exiles itself until its owner's next upkeep", gameData.id, card.getName());
    }
}
