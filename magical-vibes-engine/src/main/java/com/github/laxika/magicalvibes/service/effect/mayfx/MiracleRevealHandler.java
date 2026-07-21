package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.MiracleMayCastEffect;
import com.github.laxika.magicalvibes.model.effect.MiracleRevealEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Draw-time miracle reveal choice (CR 702.94a). Accepting reveals the card and puts the linked
 * miracle triggered ability onto the stack.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MiracleRevealHandler implements MayEffectHandlerBean {

    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return MiracleRevealEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        var card = ability.sourceCard();
        String playerName = player.getUsername();

        if (!accepted) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.textCardText(playerName + " declines to reveal ", card, " for miracle."));
            log.info("Game {} - {} declines miracle reveal for {}", gameData.id, playerName, card.getName());
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.builder()
                .text(playerName + " reveals ")
                .card(card)
                .text(" for its miracle ability.")
                .build());
        log.info("Game {} - {} reveals {} for miracle", gameData.id, playerName, card.getName());

        gameData.stack.add(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                player.getId(),
                card.getName() + "'s miracle",
                new ArrayList<>(List.of(new MiracleMayCastEffect()))
        ));
        gameData.priorityPassedBy.clear();

        inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
    }
}
