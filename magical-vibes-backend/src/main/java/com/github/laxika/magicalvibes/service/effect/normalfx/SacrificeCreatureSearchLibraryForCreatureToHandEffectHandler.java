package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureSearchLibraryForCreatureToHandEffect;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class SacrificeCreatureSearchLibraryForCreatureToHandEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;
    private final LibrarySearchSupport librarySearchSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return SacrificeCreatureSearchLibraryForCreatureToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        doResolve(gameData, entry, (SacrificeCreatureSearchLibraryForCreatureToHandEffect) effect);
    }

    private void doResolve(GameData gameData, StackEntry entry,
                                                SacrificeCreatureSearchLibraryForCreatureToHandEffect effect) {
        UUID controllerId = entry.getControllerId();
        String playerName = gameData.playerIdToName.get(controllerId);

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        List<UUID> creatureIds = new ArrayList<>();
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatureIds.add(p.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            String logEntry = playerName + " controls no creatures to sacrifice.";
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
            log.info("Game {} - {} has no creatures for sacrifice-then-search", gameData.id, playerName);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                permanentRemovalService.removePermanentToGraveyard(gameData, creature);
                String logEntry = playerName + " sacrifices " + creature.getCard().getName() + ".";
                gameBroadcastService.logAndBroadcast(gameData, logEntry);
                log.info("Game {} - {} sacrifices {}", gameData.id, playerName, creature.getCard().getName());

                librarySearchSupport.searchLibraryForCreatureToHand(gameData, controllerId);
            }
            return;
        }

        // Multiple creatures — prompt the player to choose
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.SacrificeCreatureThenSearchLibrary(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, creatureIds,
                "Choose a creature to sacrifice.");
    }
}
