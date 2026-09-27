package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AbandonSchemeUnlessDiscardOrPayLifeEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PlayerInteractionSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AbandonSchemeUnlessDiscardOrPayLifeHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PlayerInteractionSupport playerInteractionSupport;
    private final DestructionSupport destructionSupport;
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
        if (accepted && hand != null && !hand.isEmpty()) {
            gameData.discardCausedByOpponent = false;
            playerInteractionSupport.resolveDiscardCards(gameData, controllerId, 1, DiscardFollowUp.NONE);
            return;
        }

        Permanent source = gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (canPayLife(gameData, controllerId, effect.lifeCost())) {
            lifeSupport.applyLifePayment(gameData, controllerId, effect.lifeCost(), ability.sourceCard().getName());
        } else if (source != null) {
            destructionSupport.sacrificeAndLog(gameData, source, controllerId);
        }
        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    private boolean canPayLife(GameData gameData, UUID playerId, int amount) {
        return amount >= 0
                && gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= amount;
    }
}
