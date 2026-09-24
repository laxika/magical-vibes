package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.MultiPermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingForcedSacrifice;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** Resolves Replicating Terror's opponent edict and graveyard conjure effect. */
@Component
@RequiredArgsConstructor
public class EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffectHandler
        implements NormalEffectHandlerBean {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GraveyardService graveyardService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
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
            completeAfterChoices(gameData, entry, automaticChoices);
            return;
        }

        beginNextChoice(gameData, choosers, automaticChoices, entry);
    }

    public void completeChoice(GameData gameData, List<UUID> permanentIds,
            MultiPermanentChoiceContext.EachOpponentSacrificesNontokenCreatureConjuresDuplicates context) {
        List<UUID> allChoices = new ArrayList<>(context.accumulatedSacrificeIds());
        allChoices.addAll(permanentIds);

        if (!context.remainingChoosers().isEmpty()) {
            beginNextChoice(gameData, context.remainingChoosers(), allChoices, context.resolvingEntry());
            return;
        }

        completeAfterChoices(gameData, context.resolvingEntry(), allChoices);
    }

    private void beginNextChoice(GameData gameData, List<PendingForcedSacrifice> choosers,
            List<UUID> accumulatedChoices, StackEntry resolvingEntry) {
        PendingForcedSacrifice next = choosers.getFirst();
        List<PendingForcedSacrifice> remaining = List.copyOf(choosers.subList(1, choosers.size()));
        playerInputService.beginMultiPermanentChoice(gameData, next.playerId(), next.validPermanentIds(),
                next.count(), new MultiPermanentChoiceContext.EachOpponentSacrificesNontokenCreatureConjuresDuplicates(
                        remaining, accumulatedChoices, resolvingEntry),
                "Choose a nontoken creature to sacrifice.");
    }

    private void completeAfterChoices(GameData gameData, StackEntry entry, List<UUID> permanentIds) {
        List<Card> duplicates = permanentIds.stream()
                .map(permanentId -> gameQueryService.findPermanentById(gameData, permanentId))
                .filter(java.util.Objects::nonNull)
                .map(Permanent::getCard)
                .map(Card::createConjuredCopy)
                .toList();

        destructionSupport.performSimultaneousSacrifice(gameData, permanentIds);

        UUID controllerId = entry.getControllerId();
        for (Card duplicate : duplicates) {
            graveyardService.addCardToGraveyard(gameData, controllerId, duplicate);
            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(controllerId) + " conjures ", duplicate,
                    " into their graveyard."));
        }
    }

    private List<UUID> eligibleCreatureIds(GameData gameData, UUID playerId) {
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !permanent.getCard().isToken()
                        && !gameQueryService.cantBeSacrificed(gameData, permanent));
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
