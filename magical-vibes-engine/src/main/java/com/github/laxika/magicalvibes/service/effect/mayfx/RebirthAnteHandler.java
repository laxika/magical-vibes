package com.github.laxika.magicalvibes.service.effect.mayfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RebirthAnteEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PermanentChoiceHandlerService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
    private final GameLogService gameLogService;
    private final InputCompletionService inputCompletionService;
    private final ObjectProvider<PermanentChoiceHandlerService> permanentChoiceHandlerServiceProvider;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RebirthAnteEffect.class;
    }

    @Override
    public void handle(GameData gameData, Player player, boolean accepted, PendingMayAbility ability) {
        UUID playerId = ability.controllerId();     // the deciding — and potential anteing — player
        List<Card> library = gameData.playerDecks.get(playerId);

        if (accepted && library != null && !library.isEmpty()) {
            gameData.rebirthAnteAcceptedPlayerIds.add(playerId);
        } else {
            gameLogService.append(gameData, GameLog.textCardText(player.getUsername() + " declines to ante. (", ability.sourceCard(), ")"));
            log.info("Game {} - {} declines the {} ante", gameData.id, player.getUsername(),
                    ability.sourceCard().getName());
        }

        boolean decisionsPending = gameData.pendingMayAbilities.stream()
                .anyMatch(pending -> pending.effects().stream().anyMatch(RebirthAnteEffect.class::isInstance));
        if (!decisionsPending && permanentChoiceHandlerServiceProvider.getObject().answerAskingDeathReplacementsFirst(
                gameData, player, ability, accepted, () -> applyAcceptedAntes(gameData, ability))) {
            return;
        }

        inputCompletionService.sbaProcessMayAbilitiesThenAutoPass(gameData);
    }

    /**
     * CR 608.2e: every player's choice is made before the ante and life change happen, so the
     * accepted antes are performed together once the last player has decided.
     */
    private void applyAcceptedAntes(GameData gameData, PendingMayAbility ability) {
        // A life total above 20 becoming 20 is a life loss (CR 119.5) that Lich's Duel Mastery replaces with its
        // sacrifice, so every such sacrifice is asked about before the first card is anted.
        Map<UUID, Integer> newLifeTotals = new LinkedHashMap<>();
        for (UUID playerId : gameData.rebirthAnteAcceptedPlayerIds) {
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library != null && !library.isEmpty()) {
                newLifeTotals.put(playerId, 20);
            }
        }
        lifeSupport.askBeforeLifeTotalsChange(gameData, newLifeTotals);
        for (UUID playerId : List.copyOf(gameData.rebirthAnteAcceptedPlayerIds)) {
            gameData.rebirthAnteAcceptedPlayerIds.remove(playerId);
            List<Card> library = gameData.playerDecks.get(playerId);
            if (library == null || library.isEmpty()) {
                continue;
            }
            String name = gameData.playerIdToName.get(playerId);
            Card anted = library.removeFirst();
            gameData.addToAnte(playerId, anted);
            gameLogService.append(gameData, GameLog.builder().text(name + " antes ").card(anted).text(". (").card(ability.sourceCard()).text(")").build());
            log.info("Game {} - {} antes {} to {}", gameData.id, name, anted.getName(),
                    ability.sourceCard().getName());

            if (lifeSupport.applySetLifeTotal(gameData, playerId, 20)) {
                gameLogService.append(gameData, GameLog.text(name + "'s life total becomes 20."));
            }
        }
    }
}
