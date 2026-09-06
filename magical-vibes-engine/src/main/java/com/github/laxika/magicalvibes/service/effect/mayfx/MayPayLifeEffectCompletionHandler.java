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
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MayPayLifeEffectCompletionHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;
    private final InputCompletionService inputCompletionService;

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
        boolean canPay = gameQueryService.canPlayerLifeChange(gameData, playerId)
                && gameData.getLife(playerId) >= effect.lifeCost();
        if (accepted && canPay) {
            lifeSupport.applyLifePayment(gameData, playerId, effect.lifeCost(), ability.sourceCard().getName());
            StackEntry pendingEntry = gameData.pendingEffectResolutionEntry;
            if (pendingEntry != null && effect.wrapped() != null) {
                pendingEntry.insertEffectsToResolve(gameData.pendingEffectResolutionIndex,
                        List.of(effect.wrapped()));
            }
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
