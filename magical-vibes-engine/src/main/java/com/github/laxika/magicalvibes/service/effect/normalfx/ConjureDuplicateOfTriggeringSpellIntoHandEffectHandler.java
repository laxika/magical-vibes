package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedControllerEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringSpellIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Spellchain Scatter's duplicate conjure from the triggering spell's snapshot. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringSpellIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureDuplicateOfTriggeringSpellIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureDuplicateOfTriggeringSpellIntoHandEffect conjureEffect =
                (ConjureDuplicateOfTriggeringSpellIntoHandEffect) effect;
        Card triggeringSpell = gameQueryService.findCardById(gameData, entry.getTriggeringCardId());
        if (triggeringSpell == null) {
            return;
        }

        Card duplicate = triggeringSpell.createConjuredCopy();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);

        if (conjureEffect.discardAtNextEndStep()) {
            // "At the beginning of your next end step": another player's end step doesn't fire it.
            gameData.queueDelayedAction(new DelayedControllerEndStepTrigger(
                    entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(),
                    new DiscardSpecificCardEffect(duplicate.getId())));
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
        triggerCollectionService.checkConjureTriggers(gameData, entry.getControllerId(), 1);
    }
}
