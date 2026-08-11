package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerExilesCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerMayExileCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TargetPlayerMayExileCardFromGraveyardHandler implements MayEffectHandlerBean {

    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerMayExileCardFromGraveyardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID controllerId = ability.sourceControllerId() != null
                ? ability.sourceControllerId()
                : ability.controllerId();

        if (accepted) {
            gameData.stack.add(new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    ability.sourceCard(),
                    controllerId,
                    ability.sourceCard().getName() + "'s ability",
                    new ArrayList<>(List.of(new TargetPlayerExilesCardFromGraveyardEffect(0))),
                    ability.controllerId(),
                    ability.sourcePermanentId()));
        } else {
            gameData.queueMayAbility(ability.sourceCard(), controllerId,
                    new MayEffect(new DrawCardEffect(), "Draw a card?"));
        }

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
