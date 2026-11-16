package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTriggeringCardFromGraveyardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.StepTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedSelfReturnFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedSelfReturnFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedSelfReturnFromGraveyardEffect) effect;

        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s delayed return fizzles - it is no longer in a graveyard."));
            log.info("Game {} - Delayed graveyard return for {} not registered (no longer in graveyard)",
                    gameData.id, card.getName());
            return;
        }

        long graveyardEntryVersion = entry.sourceCardGraveyardEntryVersion() >= 0
                ? entry.sourceCardGraveyardEntryVersion()
                : gameData.graveyardEntryVersion(card.getId());
        UUID upkeepPlayerId = e.atNextUpkeep() && !e.atControllersNextUpkeep() ? ownerId : entry.getControllerId();
        int counterAmount = e.counterType() == null ? 0 : Math.max(0, amountEvaluationService.evaluate(gameData,
                e.counterAmount(), AmountContext.forStackEntry(entry, null)));
        StepTrigger step = e.atNextUpkeep() ? Triggers.upkeep(ActivePlayer.CHOSEN_PLAYER)
                : Triggers.endStep(ActivePlayer.ANY);
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), card, new TriggeredAbility(step,
                        new ReturnTriggeringCardFromGraveyardToBattlefieldEffect(e.tapped(), false, e.counterType(),
                                counterAmount)),
                null, null, e.atNextUpkeep() ? upkeepPlayerId : null,
                DelayedReferents.graveyardCard(card.getId(), graveyardEntryVersion)));
        gameLogService.append(gameData, GameLog.cardThen(card, !e.atNextUpkeep()
                ? " will return to the battlefield at the beginning of the next end step."
                : e.atControllersNextUpkeep()
                ? " will return to the battlefield at the beginning of the next upkeep of the player who controlled it."
                : " will return to the battlefield at the beginning of its owner's next upkeep."));
        log.info("Game {} - Delayed graveyard return registered for {} (owner {})",
                gameData.id, card.getName(), ownerId);
    }
}
