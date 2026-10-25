package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayLifeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayPayLifeEffectCompletionHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MayPayLifeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        MayPayLifeEffect effect = ability.effects().stream()
                .filter(MayPayLifeEffect.class::isInstance)
                .map(MayPayLifeEffect.class::cast)
                .findFirst()
                .orElseThrow();

        UUID playerId = ability.controllerId();
        boolean canPay = effect.lifeCost() == 0
                || (gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= effect.lifeCost());
        CardEffect continuation = accepted && canPay ? effect.wrapped() : effect.elseEffect();
        if (accepted && canPay && effect.lifeCost() > 0) {
            // Paying life is losing it (CR 119.4): a Lich's Duel Mastery sacrifice it causes is asked about first.
            if (permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                    gameData, player, ability, true, () -> lifeSupport.applyLifePayment(
                            gameData, playerId, effect.lifeCost(), ability.sourceCard().getName()))) {
                return;
            }
        }
        StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
        if (accepted && canPay && pendingEntry != null && pendingEntry.isMarkSourceOncePerTurnOnAcceptance()
                && pendingEntry.getSourcePermanentId() != null) {
            gameData.oncePerTurnTriggersFiredThisTurn.add(pendingEntry.getSourcePermanentId());
        }
        if (pendingEntry != null && continuation != null) {
            pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                    List.of(continuation));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
