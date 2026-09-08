package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExchangeControlOfSourceAndTriggeringSpellEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class ExchangeControlOfSourceAndTriggeringSpellHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExchangeControlOfSourceAndTriggeringSpellEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            gameLogService.append(gameData, GameLog.builder()
                    .text(player.getUsername() + " declines ")
                    .card(ability.sourceCard())
                    .text("'s exchange.")
                    .build());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        StackEntry entry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                ability.sourceCard(),
                ability.controllerId(),
                ability.sourceCard().getName() + "'s ability",
                new ArrayList<>(ability.effects()),
                null,
                ability.sourcePermanentId());
        entry.setNonTargeting(true);
        entry.setTriggeringCardId(ability.triggeringCardId());
        gameData.stack.add(entry);

        gameLogService.append(gameData, GameLog.builder()
                .text(player.getUsername() + " accepts ")
                .card(ability.sourceCard())
                .text("'s exchange.")
                .build());
        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
