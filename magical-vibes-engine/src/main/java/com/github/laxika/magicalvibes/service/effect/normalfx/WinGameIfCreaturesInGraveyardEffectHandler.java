package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class WinGameIfCreaturesInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameOutcomeService gameOutcomeService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return WinGameIfCreaturesInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (WinGameIfCreaturesInGraveyardEffect) effect;
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Intervening-if: check condition again on resolution
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        long creatureCount = 0;
        if (graveyard != null) {
            creatureCount = graveyard.stream()
                    .filter(c -> c.hasType(CardType.CREATURE))
                    .count();
        }

        if (creatureCount >= e.threshold()) {
            // Platinum Angel's "your opponents can't win the game". Note this is NOT a loss event:
            // a win effect ends the game immediately rather than making the opponent lose, so no
            // loss replacer (Lich's Mirror) gets a say — see GameOutcomeService.canPlayerWinGame.
            UUID opponentId = gameQueryService.getOpponentId(gameData, controllerId);
            if (!gameOutcomeService.canPlayerWinGame(gameData, controllerId)) {
                
                gameLogService.append(gameData, GameLog.builder().card(entry.getCard()).text("'s win condition is met but " + gameData.playerIdToName.get(opponentId) + " can't lose the game.").build());
                log.info("Game {} - {} win prevented — opponent can't lose", gameData.id, entry.getCard().getName());
                return;
            }

            
            gameLogService.append(gameData, GameLog.textCardText(playerName + " has " + creatureCount + " creature cards in their graveyard — " , entry.getCard(), " wins the game!"));
            log.info("Game {} - {} wins via {} ({} creatures in graveyard)",
                    gameData.id, playerName, entry.getCard().getName(), creatureCount);

            gameOutcomeService.declareWinner(gameData, controllerId);
        } else {
            gameLogService.append(gameData, GameLog.cardThen(entry.getCard(),
                    "'s ability resolves but condition is no longer met (" + creatureCount + " creature cards in graveyard)."));
            log.info("Game {} - {} intervening-if no longer met ({} creatures in graveyard, need {})",
                    gameData.id, entry.getCard().getName(), creatureCount, e.threshold());
        }
    }
}
