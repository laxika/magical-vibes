package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.cards.CardCatalog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TemporaryGlobalTriggeredAbility;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureCardNamedIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardSpecificCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.trigger.pipeline.ActionEvents;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ConjureCardNamedIntoHandEffectHandler implements NormalEffectHandlerBean {

    private final CardCatalog cardCatalog;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ConjureCardNamedIntoHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ConjureCardNamedIntoHandEffect conjure = (ConjureCardNamedIntoHandEffect) effect;
        do {
            Card card = cardCatalog.createCardIfImplemented(conjure.setCode(), conjure.collectorNumber());
            if (card == null) {
                return;
            }

            card.setConjured(true);
            card.setOwnerId(entry.getControllerId());
            card.freeze();
            gameData.addCardToHand(entry.getControllerId(), card);
            if (conjure.discardAtNextEndStep()) {
                gameData.temporaryGlobalTriggeredAbilities.add(TemporaryGlobalTriggeredAbility.atNextStep(
                        Triggers.endStep(ActivePlayer.ANY), entry.getControllerId(), entry.getCard(),
                        entry.getSourcePermanentId(), null, new DiscardSpecificCardEffect(card.getId())));
            }
            gameLogService.append(gameData, GameLog.cardThen(card, " is conjured into "
                    + gameData.playerIdToName.get(entry.getControllerId()) + "'s hand."));
            ActionEvents.conjured(gameData, entry.getControllerId(), 1);
        } while (conjure.repeatUntilHandSize() > 0
                && gameData.playerHands.getOrDefault(entry.getControllerId(), java.util.List.of()).size()
                < conjure.repeatUntilHandSize());
    }
}
