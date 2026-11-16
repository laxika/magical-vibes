package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardIntoHandAndDiscardAtNextEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
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
        conjured.setConjured(true);
        gameData.addCardToHand(entry.getControllerId(), conjured);
        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(entry.getControllerId()) + " conjures ", conjured,
                " into their hand."));
        ActionEvents.conjured(gameData, entry.getControllerId(), 1);

        Card sourceCard = entry.getCard().createRuntimeCopy();
        sourceCard.clearRuntimeSpellTargets();
        gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(Triggers.endStep(
                ActivePlayer.ANY), entry.getControllerId(), sourceCard, entry.getSourcePermanentId(),
                entry.getSourcePermanentId(), new DiscardSpecificCardEffect(conjured.getId())));
    }
}
