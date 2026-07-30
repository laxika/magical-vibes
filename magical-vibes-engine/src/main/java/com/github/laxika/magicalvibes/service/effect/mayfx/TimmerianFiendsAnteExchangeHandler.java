package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.TimmerianFiendsAnteExchangeEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.TimmerianFiendsAnteExchangeEffectHandler;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Timmerian Fiends' ante decision — "The owner of target artifact may ante the top card of their
 * library. If that player doesn't, exchange ownership of that artifact and Timmerian Fiends." The
 * artifact's owner is the decision maker; on accept the top card of their library is anted (removed
 * from the game, modelled as a move to exile) and nothing else happens, on decline the exchange is
 * performed.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class TimmerianFiendsAnteExchangeHandler implements MayEffectHandlerBean {

    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final TimmerianFiendsAnteExchangeEffectHandler exchangeEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TimmerianFiendsAnteExchangeEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID ownerId = ability.controllerId();        // the artifact's owner — the decision maker
        UUID controllerId = ability.targetCardId();   // the ability's controller — who gets the artifact
        UUID artifactId = ability.sourcePermanentId();
        List<Card> library = gameData.playerDecks.get(ownerId);

        if (accepted && library != null && !library.isEmpty()) {
            Card anted = library.removeFirst();
            gameData.addToExile(ownerId, anted);
            gameLogService.append(gameData, GameLog.builder().text(player.getUsername() + " antes ").card(anted).text(". (").card(ability.sourceCard()).text(")").build());
            log.info("Game {} - {} antes {} to {}", gameData.id, player.getUsername(), anted.getName(),
                    ability.sourceCard().getName());
        } else {
            // Declined (or no longer able to ante) — the exchange happens.
            exchangeEffectHandler.performExchange(gameData, ability.sourceCard(), controllerId, ownerId, artifactId);
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
