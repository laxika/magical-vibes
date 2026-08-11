package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSelfReturnAtNextTurnDeclareAttackersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/** Resolves the delayed self-exile and return used by Meandering Towershell. */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExileSelfReturnAtNextTurnDeclareAttackersEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSelfReturnAtNextTurnDeclareAttackersEffect.class;
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

        gameData.queueDelayedAction(new PendingExileReturn(
                card, entry.getControllerId(), true, false, TurnStep.DECLARE_ATTACKERS, 0,
                cards.size() == 1 ? List.of() : cards.subList(1, cards.size()), true, false, true));

        permanentRemovalService.removeOrphanedAuras(gameData);
        gameLogService.append(gameData, GameLog.cardThen(card,
                " is exiled. It returns tapped and attacking at the beginning of its controller's next declare-attackers step."));
        log.info("Game {} - {} exiles itself until its controller's next declare-attackers step",
                gameData.id, card.getName());
    }
}
