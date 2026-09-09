package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect;
import com.github.laxika.magicalvibes.model.effect.MayCastExiledCardWithNormalCostEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.effect.normalfx.PutCountersOnSourceEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Handles Black Widow, Super Spy's counter-or-cast choice. */
@Component
@RequiredArgsConstructor
public class ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastHandler implements MayEffectHandlerBean {

    private final PutCountersOnSourceEffectHandler putCountersOnSourceEffectHandler;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (accepted) {
            PutCountersOnSourceEffect counterEffect = new PutCountersOnSourceEffect(1, 1, 1);
            StackEntry counterEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    ability.controllerId(),
                    ability.sourceCard().getName() + "'s ability",
                    List.of(counterEffect),
                    null,
                    ability.sourcePermanentId());
            putCountersOnSourceEffectHandler.resolve(gameData, counterEntry, counterEffect);
        } else {
            ExiledCardEntry exiled = ability.targetCardId() == null
                    ? null
                    : gameData.findExiledCard(ability.targetCardId());
            if (exiled != null) {
                UUID cardId = exiled.card().getId();
                gameData.exilePlayPermissions.put(cardId, ability.controllerId());
                gameData.exilePlayPermissionsExpireEndOfTurn.add(cardId);
                gameData.exilePlayAnyManaType.add(cardId);
                gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                        ability.sourceCard(),
                        ability.controllerId(),
                        List.of(new MayCastExiledCardWithNormalCostEffect(UUID.randomUUID())),
                        "Cast " + exiled.card().getName() + "?",
                        cardId,
                        null,
                        ability.sourcePermanentId()));
            }
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
