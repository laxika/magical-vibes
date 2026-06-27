package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.state.StateTriggerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.UUID;

/**
 * Shared counter-spell helpers used by every "normal" Counter effect handler.
 *
 * <p>Extracted verbatim from {@code CounterResolutionService}; behavior is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CounterSupport {

    private final GraveyardService graveyardService;
    private final ExileService exileService;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final StateTriggerService stateTriggerService;

    public StackEntry findCounterTarget(GameData gameData, UUID targetCardId, StackEntry counterSource) {
        StackEntry targetEntry = null;
        for (StackEntry se : gameData.stack) {
            if (se.getCard().getId().equals(targetCardId)) {
                targetEntry = se;
                break;
            }
        }

        if (targetEntry == null) {
            log.info("Game {} - Counter target no longer on stack", gameData.id);
            return null;
        }

        if (gameQueryService.isUncounterable(gameData, targetEntry.getCard())) {
            log.info("Game {} - {} cannot be countered", gameData.id, targetEntry.getCard().getName());
            return null;
        }

        if (gameQueryService.isProtectedFromCounterBySpellColor(gameData, targetEntry.getControllerId(), counterSource)) {
            log.info("Game {} - {} cannot be countered by {} spells",
                    gameData.id, targetEntry.getCard().getName(),
                    counterSource.getCard().getColor().name().toLowerCase());
            return null;
        }

        return targetEntry;
    }

    public void counterSpell(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        boolean isAbility = target.getEntryType() == StackEntryType.ACTIVATED_ABILITY
                || target.getEntryType() == StackEntryType.TRIGGERED_ABILITY;

        if (!target.isCopy() && !isAbility) {
            graveyardService.addCardToGraveyard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = isAbility
                ? target.getCard().getName() + "'s ability is countered."
                : target.getCard().getName() + " is countered.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }

    public void counterSpellAndExile(GameData gameData, StackEntry source, StackEntry target) {
        gameData.stack.remove(target);

        stateTriggerService.cleanupResolvedStateTrigger(gameData, target);

        if (!target.isCopy()) {
            exileService.exileCard(gameData, target.getControllerId(), target.getCard());
        }

        String logMsg = target.getCard().getName() + " is countered and exiled.";
        gameBroadcastService.logAndBroadcast(gameData, logMsg);
        log.info("Game {} - {} countered and exiled {}", gameData.id, source.getCard().getName(), target.getCard().getName());
    }

    public boolean sharesCardType(Card card, Set<CardType> types) {
        if (types.contains(card.getType())) return true;
        for (CardType additionalType : card.getAdditionalTypes()) {
            if (types.contains(additionalType)) return true;
        }
        return false;
    }
}
