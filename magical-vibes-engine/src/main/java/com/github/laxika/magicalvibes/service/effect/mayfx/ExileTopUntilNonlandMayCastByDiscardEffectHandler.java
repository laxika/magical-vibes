package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DiscardFollowUp;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CastExiledCardWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandMayCastByDiscardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Handles the discard-backed may-cast choice for The Infamous Cruelclaw.
 */
@Component("mayExileTopUntilNonlandMayCastByDiscardEffectHandler")
@RequiredArgsConstructor
public class ExileTopUntilNonlandMayCastByDiscardEffectHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTopUntilNonlandMayCastByDiscardEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        if (!accepted) {
            gameLogService.append(gameData, GameLog.textCardText(
                    player.getUsername() + " declines to cast the exiled card with ",
                    ability.sourceCard(), "'s ability."));
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        ExiledCardEntry exiled = ability.targetCardId() == null
                ? null
                : gameData.findExiledCard(ability.targetCardId());
        List<Card> hand = gameData.playerHands.get(player.getId());
        if (exiled == null || hand == null || hand.isEmpty()) {
            inputCompletionService.processMayAbilitiesThenAutoPass(gameData);
            return;
        }

        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            validIndices.add(i);
        }

        gameData.discardCausedByOpponent = false;
        playerInputService.beginDiscardChoice(
                gameData,
                player.getId(),
                validIndices,
                ability.sourceCard().getName() + " — Choose a card to discard.",
                1,
                DiscardFollowUp.thenEffect(
                        ability.sourceCard(),
                        new CastExiledCardWithoutPayingManaCostEffect(ability.targetCardId())));
        gameLogService.append(gameData, GameLog.text(
                player.getUsername() + " is choosing a card to discard to cast " + exiled.card().getName() + "."));
    }
}
