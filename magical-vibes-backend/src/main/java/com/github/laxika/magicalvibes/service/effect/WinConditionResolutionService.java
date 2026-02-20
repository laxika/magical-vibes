package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.WinGameIfCreaturesInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class WinConditionResolutionService implements EffectHandlerProvider {

    private final GameHelper gameHelper;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public void registerHandlers(EffectHandlerRegistry registry) {
        registry.register(WinGameIfCreaturesInGraveyardEffect.class,
                (gd, entry, effect) -> resolveWinGameIfCreaturesInGraveyard(gd, entry, (WinGameIfCreaturesInGraveyardEffect) effect));
    }

    private void resolveWinGameIfCreaturesInGraveyard(GameData gameData, StackEntry entry, WinGameIfCreaturesInGraveyardEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        // Intervening-if: check condition again on resolution
        List<Card> graveyard = gameData.playerGraveyards.get(controllerId);
        long creatureCount = 0;
        if (graveyard != null) {
            creatureCount = graveyard.stream()
                    .filter(c -> c.getType() == CardType.CREATURE
                            || c.getAdditionalTypes().contains(CardType.CREATURE))
                    .count();
        }

        if (creatureCount >= effect.threshold()) {
            String logEntry = playerName + " has " + creatureCount + " creature cards in their graveyard — " + entry.getCard().getName() + " wins the game!";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} wins via {} ({} creatures in graveyard)",
                    gameData.id, playerName, entry.getCard().getName(), creatureCount);

            gameHelper.declareWinner(gameData, controllerId);
        } else {
            String logEntry = entry.getCard().getName() + "'s ability resolves but condition is no longer met (" + creatureCount + " creature cards in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} intervening-if no longer met ({} creatures in graveyard, need {})",
                    gameData.id, entry.getCard().getName(), creatureCount, effect.threshold());
        }
    }
}

