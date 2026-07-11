package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentChoosesCreatureToDestroyEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves {@link OpponentChoosesCreatureToDestroyEffect}: an opponent of the controller chooses any
 * creature on the battlefield and it is destroyed (Diaochan, Artful Beauty's second destruction).
 * With 0 creatures nothing happens; with exactly 1 it is destroyed automatically; with 2+ the
 * opponent picks. Reuses the {@link PermanentChoiceContext.DestroyChosenCreature} choice flow.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OpponentChoosesCreatureToDestroyEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameBroadcastService gameBroadcastService;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return OpponentChoosesCreatureToDestroyEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        // The controller chooses which opponent makes the choice (rulings). In a two-player game there
        // is a single opponent; pick the first opponent in turn order for determinism.
        UUID opponentId = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (!playerId.equals(controllerId)) {
                opponentId = playerId;
                break;
            }
        }
        if (opponentId == null) {
            return;
        }

        String cardName = entry.getCard().getName();
        List<UUID> creatureIds = new ArrayList<>();
        for (Map.Entry<UUID, List<Permanent>> bf : gameData.playerBattlefields.entrySet()) {
            for (Permanent perm : bf.getValue()) {
                if (gameQueryService.isCreature(gameData, perm)) {
                    creatureIds.add(perm.getId());
                }
            }
        }

        if (creatureIds.isEmpty()) {
            gameBroadcastService.logAndBroadcast(gameData,
                    cardName + " resolves but there are no creatures to destroy.");
            log.info("Game {} - {} resolves with no creatures to destroy", gameData.id, cardName);
            return;
        }

        if (creatureIds.size() == 1) {
            Permanent creature = gameQueryService.findPermanentById(gameData, creatureIds.getFirst());
            if (creature != null) {
                destructionSupport.tryDestroyAndLog(gameData, creature, cardName);
            }
            return;
        }

        // Multiple creatures — prompt the opponent to choose which one to destroy.
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.DestroyChosenCreature(opponentId, cardName));
        playerInputService.beginPermanentChoice(gameData, opponentId, creatureIds,
                "Choose a creature to destroy.");
    }
}
