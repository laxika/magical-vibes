package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles Black Widow, Super Spy's counter-or-cast choice. */
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastHandler implements MayEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        Permanent source = ability.sourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, ability.sourcePermanentId());
        if (accepted && source != null
                && !gameQueryService.cantHavePlusOnePlusOneCounters(gameData, source)) {
            PutCountersEffect counterEffect = PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE);
            StackEntry counterEntry = StackEntry.carrier(ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    List.of(counterEffect),
                    null,
                    ability.sourcePermanentId());
            permanentCounterSupport.placeCounterOnPermanent(gameData, counterEntry, source,
                    CounterType.PLUS_ONE_PLUS_ONE, 1);
        } else {
            ExiledCardEntry exiled = ability.targetCardId() == null
                    ? null
                    : gameData.findExiledCard(ability.targetCardId());
            if (exiled != null) {
                UUID cardId = exiled.card().getId();
                gameData.grantExilePlayPermission(cardId, ability.controllerId(), Duration.UNTIL_END_OF_TURN);
                gameData.grantExileAnyManaType(cardId, Duration.UNTIL_END_OF_TURN);

            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
