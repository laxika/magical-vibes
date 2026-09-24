package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedEndStepTrigger;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoHandAndDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConjureCardIntoHandAndDiscardAtNextEndStepEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardIntoHandAndDiscardAtNextEndStepEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ConjureCardIntoHandAndDiscardAtNextEndStepEffect) effect;
        CardSet set = CardSet.findByCode(e.setCode());
        if (set == null) {
            throw new IllegalArgumentException("Unknown card set code: " + e.setCode());
        }

        Card conjured = cardCatalog.findByCollectorNumber(set, e.collectorNumber()).createCard();
        gameData.addCardToHand(entry.getControllerId(), conjured);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " conjures ", conjured,
                " into their hand."));

        Card sourceCard = entry.getCard().createRuntimeCopy();
        sourceCard.clearRuntimeSpellTargets();
        gameData.queueDelayedAction(new DelayedEndStepTrigger(
                entry.getControllerId(), sourceCard, entry.getSourcePermanentId(),
                entry.getSourcePermanentId(), new DiscardSpecificCardEffect(conjured.getId())));
    }
}
