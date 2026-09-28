package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an each-player creature sacrifice with simultaneous sacrifice after all choices. */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesCreatureEffectHandler implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesCreatureEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        List<UUID> automaticChoices = new ArrayList<>();

        for (UUID playerId : apnapPlayers(gameData)) {
            if (!gameQueryService.canEffectCauseSacrifice(gameData, playerId, entry.getControllerId())) {
                continue;
            }

            List<UUID> creatureIds = eligibleCreatureIds(gameData, playerId);
            if (creatureIds.isEmpty()) {
                continue;
            }
            if (creatureIds.size() == 1) {
                automaticChoices.add(creatureIds.getFirst());
            } else {
                choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
            }
        }

        if (choosers.isEmpty()) {
            completeAfterChoices(gameData, automaticChoices);
            return;
        }

        beginNextChoice(gameData, choosers, automaticChoices, entry);
    }

    /** Completes one player choice and continues the APNAP choice sequence. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachPlayerSacrificesCreature context) {
        List<UUID> allChoices = new ArrayList<>(context.accumulatedSacrificeIds());
        allChoices.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            beginNextChoice(gameData, context.remainingChoosers(), allChoices, context.resolvingEntry());
            return;
        }

        completeAfterChoices(gameData, allChoices);
    }

    private void beginNextChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                 List<UUID> accumulatedChoices, StackEntry resolvingEntry) {
        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remaining = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(), new MultiPermanentChoiceContext.EachPlayerSacrificesCreature(
                        remaining, accumulatedChoices, resolvingEntry),
                "Choose a creature to sacrifice.");
    }

    private void completeAfterChoices(GameData gameData, List<UUID> permanentIds) {
        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
        permanentRemovalService.removeOrphanedAuras(gameData);
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayerIds = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayerIds.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(orderedPlayerIds.subList(activeIndex, orderedPlayerIds.size()));
            rotated.addAll(orderedPlayerIds.subList(0, activeIndex));
            return rotated;
        }
        return orderedPlayerIds;
    }
}
