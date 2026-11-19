package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringCardIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves a duplicate of the card that caused a trigger into its controller's hand, optionally scheduling that
 * duplicate's discard at the controller's next end step.
 */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringCardIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringCardIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureDuplicateOfTriggeringCardIntoHandEffect conjureEffect =
                (ConjureDuplicateOfTriggeringCardIntoHandEffect) effect;
        Card triggeringCard = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringCard == null) {
            return;
        }

        Card duplicate = triggeringCard.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);

        if (conjureEffect.discardAtNextEndStep()) {
            // "At the beginning of your next end step": another player's end step doesn't fire it.
            gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                    Triggers.endStep(ActivePlayer.YOU), entry.getControllerId(), entry.getCard(),
                    entry.getSourcePermanentId(), null, new DiscardSpecificCardEffect(duplicate.getId())));
        }

        gameLogService.append(gameData,
                GameLog.cardThen(entry.getCard(), " conjures a duplicate of "
                        + triggeringCard.getName() + " into their hand."));
        ActionEvents.conjured(gameData, entry.getControllerId(), 1);
    }
}
