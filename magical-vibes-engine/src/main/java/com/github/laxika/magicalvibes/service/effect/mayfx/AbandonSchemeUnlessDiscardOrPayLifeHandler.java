package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeUnlessDiscardOrPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AbandonSchemeUnlessDiscardOrPayLifeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves the controller's choice for an abandon-unless scheme: accepting the discard prompt
 * discards a card, declining it pays life when that is possible, and otherwise the scheme is abandoned.
 * When the discard prompt is not offered, accepting pays the life cost.
 */
@Component
@RequiredArgsConstructor
public class AbandonSchemeUnlessDiscardOrPayLifeHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final AbandonSchemeUnlessDiscardOrPayLifeEffectHandler abandonHandler;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AbandonSchemeUnlessDiscardOrPayLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        AbandonSchemeUnlessDiscardOrPayLifeEffect effect = ability.effects().stream()
                .filter(AbandonSchemeUnlessDiscardOrPayLifeEffect.class::isInstance)
                .map(AbandonSchemeUnlessDiscardOrPayLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID controllerId = ability.controllerId();
        List<Card> hand = gameData.playerHands.get(controllerId);
        boolean canDiscard = hand != null && !hand.isEmpty();
        boolean canPayLife = abandonHandler.canPayLife(gameData, controllerId, effect.lifeCost());

        if (accepted && canDiscard) {
            abandonHandler.discard(gameData, controllerId);
            return;
        }

        boolean payLife = accepted ? canPayLife : canDiscard && canPayLife;
        if (payLife) {
            lifeSupport.applyLifePayment(gameData, controllerId, effect.lifeCost(), ability.sourceCard().getName());
        } else {
            Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
            if (source != null) {
                abandonHandler.abandon(gameData, source);
            }
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
