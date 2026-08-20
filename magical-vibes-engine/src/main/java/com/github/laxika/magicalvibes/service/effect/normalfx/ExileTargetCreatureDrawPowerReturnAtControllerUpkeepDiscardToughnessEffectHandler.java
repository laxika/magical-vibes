package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffect;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/** Resolves Vanish into Memory's exile, draw, and delayed return sequence. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;
    private final PlayerInteractionSupport playerInteractionSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetCreatureDrawPowerReturnAtControllerUpkeepDiscardToughnessEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream().findFirst().orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target == null) {
            return;
        }

        int power = Math.max(0, gameQueryService.getEffectivePower(gameData, target));
        UUID currentControllerId = gameQueryService.findPermanentController(gameData, targetId);
        UUID ownerId = gameData.stolenCreatures.getOrDefault(targetId, currentControllerId);
        List<Card> cards = target.cardsLeavingBattlefield();
        if (cards.isEmpty() || ownerId == null || !permanentRemovalService.removePermanentToExile(gameData, target)) {
            return;
        }

        Card card = cards.getFirst();
        gameData.queueDelayedAction(PendingExileReturn.forControllerNextUpkeepWithToughnessDiscard(
                card,
                ownerId,
                entry.getControllerId(),
                entry.getCard(),
                cards.size() == 1 ? List.of() : cards.subList(1, cards.size())));
        permanentRemovalService.removeOrphanedAuras(gameData);

        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled. It will return at the beginning of the controller's next upkeep."));
        log.info("Game {} - {} exiles {}; it will return at the controller's next upkeep",
                gameData.id, entry.getCard().getName(), card.getName());

        playerInteractionSupport.applyDrawCards(gameData, entry.getControllerId(), power);
    }
}
