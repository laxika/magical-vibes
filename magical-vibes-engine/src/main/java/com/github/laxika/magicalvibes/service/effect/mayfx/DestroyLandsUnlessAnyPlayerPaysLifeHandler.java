package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyLandsUnlessAnyPlayerPaysLifeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestroyLandsUnlessAnyPlayerPaysLifeEffectHandler;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/** Handles one player's choice to pay life to keep a land during Cleansing. */
@Component
@RequiredArgsConstructor
public class DestroyLandsUnlessAnyPlayerPaysLifeHandler implements MayEffectHandlerBean {

    private final DestroyLandsUnlessAnyPlayerPaysLifeEffectHandler effectHandler;
    private final GameLogService gameLogService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Autowired @Lazy
    private LifeSupport lifeSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DestroyLandsUnlessAnyPlayerPaysLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var effect = ability.effects().stream()
                .filter(DestroyLandsUnlessAnyPlayerPaysLifeEffect.class::isInstance)
                .map(DestroyLandsUnlessAnyPlayerPaysLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();

        boolean paid = accepted && effectHandler.canPayLife(gameData, player.getId(), effect.lifeCost());
        if (paid) {
            // Paying life is losing it (CR 119.4): a Lich's Duel Mastery sacrifice it causes is asked about first.
            // The payment is the printed amount; a life-loss multiplier (Bloodletter of Aclazotz) applies once, to
            // the life the payment makes the player lose.
            if (permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> lifeSupport.applyLifePayment(
                            gameData, player.getId(), effect.lifeCost(), ability.sourceCard().getName()))) {
                return;
            }
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " pays " + effect.lifeCost() + " life to prevent ",
                    ability.sourceCard(), "'s land from being destroyed."));
        }

        effectHandler.continueAfterDecision(gameData, effect, ability.sourceCard(), paid, player.getId());
    }
}
