package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an each-player creature sacrifice followed by a power-sized token creation. */
@Component
@RequiredArgsConstructor
public class EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachPlayerSacrificesCreatureCreateTokenEqualToTotalPowerEffect) effect;
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
            choosers.add(new PendingForcedSacrifice(playerId, 1, creatureIds));
            if (creatureIds.size() == 1) {
                automaticChoices.add(creatureIds.getFirst());
            }
        }

        if (choosers.stream().allMatch(choice -> choice.validPermanentIds().size() <= choice.count())) {
            completeAfterChoices(gameData, entry, e.tokenTemplate(), automaticChoices);
            return;
        }

        beginNextChoice(gameData, choosers, List.of(), e.tokenTemplate(), entry);
    }

    /** Completes one player choice and continues the APNAP choice sequence. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachPlayerSacrificesCreatureCreateTokenEqualToTotalPower context) {
        List<UUID> allChoices = new ArrayList<>(context.accumulatedSacrificeIds());
        allChoices.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            beginNextChoice(gameData, context.remainingChoosers(), allChoices,
                    context.tokenTemplate(), context.resolvingEntry());
            return;
        }

        completeAfterChoices(gameData, context.resolvingEntry(), context.tokenTemplate(), allChoices);
    }

    private void beginNextChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
                                 List<UUID> accumulatedChoices, CreateTokenEffect tokenTemplate,
                                 StackEntry resolvingEntry) {
        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remaining = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(),
                new MultiPermanentChoiceContext.EachPlayerSacrificesCreatureCreateTokenEqualToTotalPower(
                        remaining, accumulatedChoices, tokenTemplate, resolvingEntry),
                "Choose a creature to sacrifice.");
    }

    private void completeAfterChoices(GameData gameData, StackEntry entry, CreateTokenEffect tokenTemplate,
                                      List<UUID> permanentIds) {
        int totalPower = totalEffectivePower(gameData, permanentIds);
        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
        permanentRemovalService.removeOrphanedAuras(gameData);
        createTokenEffectHandler.resolve(gameData, entry,
                tokenTemplate.withPowerToughness(totalPower, totalPower));
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeSacrificed(gameData, permanent));
    }

    private int totalEffectivePower(GameData gameData, List<UUID> permanentIds) {
        int total = 0;
        for (UUID permanentId : permanentIds) {
            Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
            if (permanent != null) {
                total += gameQueryService.getEffectivePower(gameData, permanent);
            }
        }
        return total;
    }

    private List<UUID> apnapPlayers(GameData gameData) {
        List<UUID> orderedPlayers = new ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = orderedPlayers.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            List<UUID> rotated = new ArrayList<>(orderedPlayers.subList(activeIndex, orderedPlayers.size()));
            rotated.addAll(orderedPlayers.subList(0, activeIndex));
            return rotated;
        }
        return orderedPlayers;
    }
}
