package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDuplicateOfTriggeringSpellIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Spellchain Scatter's duplicate conjure from the triggering spell's snapshot. */
@Component
@RequiredArgsConstructor
public class ConjureDuplicateOfTriggeringSpellIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;

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

        Card duplicate = triggeringSpell.createRuntimeCopyWithNewId();
        duplicate.setOwnerId(entry.getControllerId());
        duplicate.setToken(true);
        duplicate.setTokenCard(true);
        duplicate.freeze();
        gameData.addCardToHand(entry.getControllerId(), duplicate);

        if (conjureEffect.discardAtNextEndStep()) {
            gameData.queueDelayedAction(new DelayedEndStepTrigger(
                    entry.getControllerId(), entry.getCard(), entry.getSourcePermanentId(), null,
                    new DiscardSpecificCardEffect(duplicate.getId())));
        }

        gameLogService.append(gameData, GameLog.textCardText(
                "A duplicate of ", duplicate, " is conjured into your hand."));
    }
}
