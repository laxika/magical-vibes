package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedReturnSourceTransformedEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.DelayedReferents;
import com.github.laxika.magicalvibes.model.effect.ReturnSourceTransformedFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@Slf4j
@Component
@RequiredArgsConstructor
public class RegisterDelayedReturnSourceTransformedEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RegisterDelayedReturnSourceTransformedEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (RegisterDelayedReturnSourceTransformedEffect) effect;

        Card card = entry.getCard();
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, card.getId());
        if (ownerId == null) {
            gameLogService.append(gameData, GameLog.cardThen(card, "'s delayed return fizzles - it is no longer in a graveyard."));
            log.info("Game {} - Delayed transformed return for {} not registered (no longer in graveyard)",
                    gameData.id, card.getName());
            return;
        }

        UUID returnControllerId = e.underOwnerControl() ? ownerId : entry.getControllerId();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                entry.getControllerId(), card,
                new TriggeredAbility(Triggers.endStep(ActivePlayer.ANY),
                        new ReturnSourceTransformedFromGraveyardEffect(false, e.underOwnerControl())),
                entry.getSourcePermanentId(), null, null,
                DelayedReferents.graveyardCard(card.getId(), gameData.graveyardEntryVersion(card.getId()))));
        String playerName = gameData.playerIdToName.get(returnControllerId);
        gameLogService.append(gameData, GameLog.builder().card(card).text(" will return to the battlefield transformed under " + playerName + "'s control at the beginning of the next end step.").build());
        log.info("Game {} - Delayed transformed return registered for {} (owner {}, controller {})",
                gameData.id, card.getName(), ownerId, returnControllerId);
    }
}
