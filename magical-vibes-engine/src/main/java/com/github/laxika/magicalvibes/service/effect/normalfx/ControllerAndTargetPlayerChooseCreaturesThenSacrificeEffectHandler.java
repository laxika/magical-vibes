package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerAndTargetPlayerChooseCreaturesThenSacrificeEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Malik's private creature choices and simultaneous sacrifice. */
@Component
@RequiredArgsConstructor
public class ControllerAndTargetPlayerChooseCreaturesThenSacrificeEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ControllerAndTargetPlayerChooseCreaturesThenSacrificeEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        UUID targetPlayerId = entry.getTargetId();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        List<UUID> automaticChoices = new ArrayList<>();

        addChooser(gameData, controllerId, controllerId, choosers, automaticChoices);
        addChooser(gameData, targetPlayerId, controllerId, choosers, automaticChoices);

        if (choosers.stream().allMatch(choice -> choice.validPermanentIds().size() <= choice.count())) {
            completeAfterChoices(gameData, automaticChoices);
            return;
        }

        beginNextChoice(gameData, choosers, automaticChoices, entry);
    }

    /** Completes one private choice and prompts the other player, if needed. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.ControllerAndTargetPlayerChooseCreaturesThenSacrifice context) {
        List<UUID> allChoices = new ArrayList<>(context.accumulatedSacrificeIds());
        allChoices.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            beginNextChoice(gameData, context.remainingChoosers(), allChoices, context.resolvingEntry());
            return;
        }

        completeAfterChoices(gameData, allChoices);
    }

    private void addChooser(GameData gameData, UUID playerId, UUID sourceControllerId,
                            List<PendingForcedSacrifice> choosers, List<UUID> automaticChoices) {
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return;
        }

        List<UUID> creatureIds = destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
        if (creatureIds.isEmpty()) {
            return;
        }
        if (creatureIds.size() == 1) {
            automaticChoices.add(creatureIds.getFirst());
        } else {
            choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
        }
    }

    private void beginNextChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                 List<UUID> accumulatedChoices, StackEntry resolvingEntry) {
        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remaining = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(), 1,
                new MultiPermanentChoiceContext.ControllerAndTargetPlayerChooseCreaturesThenSacrifice(
                        remaining, accumulatedChoices, resolvingEntry),
                "Choose a creature to sacrifice.");
    }

    private void completeAfterChoices(GameData gameData, List<UUID> permanentIds) {
        if (!permanentIds.isEmpty()) {
            destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }
}
