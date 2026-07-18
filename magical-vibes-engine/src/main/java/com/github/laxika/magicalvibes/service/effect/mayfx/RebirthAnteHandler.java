package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RebirthAnteEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Rebirth's per-player ante decision — "Each player may ante the top card of their library. If a
 * player does, that player's life total becomes 20." The deciding player (carried in the ability's
 * {@code controllerId}) is the potential anteing player. On accept, the top card of their library is
 * anted (removed from the game, modelled as a move to exile) and their life is set to 20 (CR 119.5);
 * on decline, nothing happens.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RebirthAnteHandler implements MayEffectHandlerBean {

    private final LifeSupport lifeSupport;
    private final GameBroadcastService gameBroadcastService;
    private final InputCompletionService inputCompletionService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RebirthAnteEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID playerId = ability.controllerId();     // the deciding — and potential anteing — player
        List<Card> library = gameData.playerDecks.get(playerId);

        if (accepted && library != null && !library.isEmpty()) {
            Card anted = library.removeFirst();
            gameData.addToExile(playerId, anted);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    player.getUsername() + " antes " + anted.getName() + ". (" + ability.sourceCard().getName() + ")"));
            log.info("Game {} - {} antes {} to {}", gameData.id, player.getUsername(), anted.getName(),
                    ability.sourceCard().getName());

            if (lifeSupport.applySetLifeTotal(gameData, playerId, 20)) {
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                        player.getUsername() + "'s life total becomes 20."));
            }
        } else {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(
                    player.getUsername() + " declines to ante. (" + ability.sourceCard().getName() + ")"));
            log.info("Game {} - {} declines the {} ante", gameData.id, player.getUsername(),
                    ability.sourceCard().getName());
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }
}
