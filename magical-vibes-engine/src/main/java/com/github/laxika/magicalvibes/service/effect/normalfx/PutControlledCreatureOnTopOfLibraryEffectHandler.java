package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PutControlledCreatureOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * "Put a creature you control on top of its owner's library." (Nulltread Gargantuan.)
 *
 * <p>Non-targeted, mandatory choice made at resolution. The controller chooses one creature they
 * control — the source itself is a legal choice, so with no other creature they must top the source.
 * With exactly one candidate the pick is forced and made here; with several the controller is
 * prompted via {@link PermanentChoiceContext.PutControlledCreatureOnTopOfLibrary}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PutControlledCreatureOnTopOfLibraryEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return PutControlledCreatureOnTopOfLibraryEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();

        List<Permanent> creatures = new ArrayList<>();
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield != null) {
            for (Permanent p : battlefield) {
                if (gameQueryService.isCreature(gameData, p)) {
                    creatures.add(p);
                }
            }
        }

        if (creatures.isEmpty()) {
            log.info("Game {} - {} ETB puts no creature on top of library (controller has no creatures)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        if (creatures.size() == 1) {
            // Forced choice — the controller's only creature (the source itself when nothing else is out).
            putOnTopOfLibrary(gameData, creatures.getFirst());
            permanentRemovalService.removeOrphanedAuras(gameData);
            return;
        }

        List<UUID> validIds = creatures.stream().map(Permanent::getId).toList();
        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.PutControlledCreatureOnTopOfLibrary(controllerId));
        playerInputService.beginPermanentChoice(gameData, controllerId, new ArrayList<>(validIds),
                "Choose a creature you control to put on top of its owner's library.");
    }

    private void putOnTopOfLibrary(GameData gameData, Permanent target) {
        if (permanentRemovalService.removePermanentToLibraryTop(gameData, target)) {
            gameBroadcastService.logAndBroadcast(gameData,
                    GameLog.cardThen(target.getCard(), " is put on top of its owner's library."));
            log.info("Game {} - {} put on top of library", gameData.id, target.getCard().getName());
        }
    }
}
