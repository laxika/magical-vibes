package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.AmuletOfQuozAnteEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.AmuletOfQuozAnteEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Amulet of Quoz's ante decision — "Target opponent may ante the top card of their library. If they
 * don't, you flip a coin. If you win the flip, that player loses the game. If you lose the flip, you
 * lose the game." The targeted opponent is the decision maker; on accept the top card of their library
 * is anted (removed from the game, modelled as a move to exile) and nothing else happens, on decline
 * the ability's controller flips the coin.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AmuletOfQuozAnteHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final AmuletOfQuozAnteEffectHandler anteEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return AmuletOfQuozAnteEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID opponentId = ability.controllerId();     // the targeted opponent — the decision maker
        UUID controllerId = ability.targetCardId();   // the ability's controller — the coin flipper
        List<Card> library = gameData.playerDecks.get(opponentId);

        if (accepted && library != null && !library.isEmpty()) {
            Card anted = library.removeFirst();
            gameData.addToAnte(opponentId, anted);
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " antes ").card(anted).text(". (").card(ability.sourceCard()).text(")").build());
            log.info("Game {} - {} antes {} to {}", gameData.id, player.getUsername(), anted.getName(),
                    ability.sourceCard().getName());
        } else {
            // Declined (or no longer able to ante) — the controller flips the coin.
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " doesn't ante. (", ability.sourceCard(), ")"));
            anteEffectHandler.performFlip(gameData, ability.sourceCard(), controllerId, opponentId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
