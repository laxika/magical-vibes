package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesCreatureCreateTokensEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves an each-opponent creature sacrifice followed by one token per sacrificed creature. */
@Component
@RequiredArgsConstructor
public class EachOpponentSacrificesCreatureCreateTokensEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final PermanentRemovalService permanentRemovalService;
    private final CreateTokenEffectHandler createTokenEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesCreatureCreateTokensEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (EachOpponentSacrificesCreatureCreateTokensEffect) effect;
        UUID controllerId = entry.getControllerId();
        List<PendingForcedSacrifice> choosers = new ArrayList<>();
        List<UUID> automaticChoices = new ArrayList<>();

        for (UUID playerId : apnapPlayers(gameData)) {
            if (playerId.equals(controllerId)
                    || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, controllerId)) {
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
            completeAfterChoices(gameData, entry, e.tokenTemplate(), automaticChoices);
            return;
        }

        beginNextChoice(gameData, choosers, automaticChoices, e.tokenTemplate(), entry);
    }

    /** Completes one opponent's choice and continues the APNAP choice sequence. */
    public void completeChoice(GameData gameData, List<UUID> permanentIds,
                               MultiPermanentChoiceContext.EachOpponentSacrificesCreatureCreateTokens context) {
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
                next.count(), new MultiPermanentChoiceContext.EachOpponentSacrificesCreatureCreateTokens(
                        remaining, accumulatedChoices, tokenTemplate, resolvingEntry),
                "Choose a creature to sacrifice.");
    }

    private void completeAfterChoices(GameData gameData, StackEntry entry, CreateTokenEffect tokenTemplate,
                                      List<UUID> permanentIds) {
        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);
        permanentRemovalService.removeOrphanedAuras(gameData);
        createTokenEffectHandler.resolve(gameData, entry, tokenTemplate.withAmount(permanentIds.size()));
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
