package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.DepartureResult;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardChoiceDestination;
import com.github.laxika.magicalvibes.model.PendingGraveyardReturnChoice;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ContinueAfterDepartureEffect;
import com.github.laxika.magicalvibes.model.effect.InfernalOfferingEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.service.DrawService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ContinuesAfterDeparture;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/** Resolves Infernal Offering's resolution-time opponent and creature choices. */
@Component
@RequiredArgsConstructor
public class InfernalOfferingEffectHandler implements NormalEffectHandlerBean, ContinuesAfterDeparture {

    private final DestructionSupport destructionSupport;
    private final GameQueryService gameQueryService;
    private final PlayerInputService playerInputService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final StepDepartureSupport stepDepartureSupport;
    private final DrawService drawService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return InfernalOfferingEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        InfernalOfferingEffect offering = (InfernalOfferingEffect) effect;
        List<UUID> opponents = opponentsOf(gameData, entry.getControllerId());
        if (opponents.isEmpty()) {
            return;
        }

        PermanentChoiceContext.InfernalOfferingOpponentChoice context =
                new PermanentChoiceContext.InfernalOfferingOpponentChoice(
                        entry.getControllerId(), offering.sacrificeMode(), entry.getCard().getName());
        int nextEffectIndex = entry.getEffectsToResolve().indexOf(effect) + 1;
        if (opponents.size() == 1) {
            continueAfterOpponentChoice(gameData, entry, offering.sacrificeMode(), opponents.getFirst(),
                    nextEffectIndex);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(context);
        playerInputService.beginPlayerChoice(gameData, entry.getControllerId(), opponents,
                entry.getCard().getName() + " — choose an opponent.");
    }

    public void completeOpponentChoice(GameData gameData, UUID chosenOpponentId,
                                       PermanentChoiceContext.InfernalOfferingOpponentChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null || !opponentsOf(gameData, context.controllerId()).contains(chosenOpponentId)) {
            throw new IllegalStateException("Invalid opponent choice");
        }

        continueAfterOpponentChoice(gameData, entry, context.sacrificeMode(), chosenOpponentId,
                gameData.pendingEffectResolutionIndex);
    }

    public void completeCreatureChoice(GameData gameData, UUID permanentId,
                                       PermanentChoiceContext.InfernalOfferingCreatureChoice context) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            throw new IllegalStateException("No Infernal Offering resolution is pending");
        }

        List<UUID> validCreatureIds = creatureIds(gameData, context.choosingPlayerId(), context.controllerId());
        if (!validCreatureIds.contains(permanentId)) {
            throw new IllegalStateException("Invalid creature choice: " + permanentId);
        }

        List<UUID> chosenCreatureIds = new ArrayList<>(context.chosenCreatureIds());
        chosenCreatureIds.add(permanentId);
        List<UUID> chosenPlayerIds = new ArrayList<>(context.chosenPlayerIds());
        chosenPlayerIds.add(context.choosingPlayerId());
        continueCreatureChoices(gameData, entry, context.controllerId(), context.opponentId(),
                context.remainingChooserIds(), chosenCreatureIds, chosenPlayerIds,
                gameData.pendingEffectResolutionIndex);
    }

    private void continueAfterOpponentChoice(GameData gameData, StackEntry entry, boolean sacrificeMode,
                                              UUID opponentId, int insertionIndex) {
        entry.setTargetId(opponentId);
        if (sacrificeMode) {
            beginSacrificeChoices(gameData, entry, opponentId, insertionIndex);
        } else {
            queueGraveyardReturns(gameData, entry.getControllerId(), opponentId);
        }
    }

    private void beginSacrificeChoices(GameData gameData, StackEntry entry, UUID opponentId,
                                       int insertionIndex) {
        List<UUID> chooserIds = new ArrayList<>();
        List<UUID> chosenCreatureIds = new ArrayList<>();
        List<UUID> chosenPlayerIds = new ArrayList<>();
        for (UUID playerId : List.of(entry.getControllerId(), opponentId)) {
            List<UUID> creatureIds = creatureIds(gameData, playerId, entry.getControllerId());
            if (creatureIds.size() == 1) {
                chosenCreatureIds.add(creatureIds.getFirst());
                chosenPlayerIds.add(playerId);
            } else if (creatureIds.size() > 1) {
                chooserIds.add(playerId);
            }
        }

        continueCreatureChoices(gameData, entry, entry.getControllerId(), opponentId, chooserIds,
                chosenCreatureIds, chosenPlayerIds, insertionIndex);
    }

    private void continueCreatureChoices(GameData gameData, StackEntry entry, UUID controllerId,
                                         UUID opponentId, List<UUID> remainingChooserIds,
                                         List<UUID> chosenCreatureIds, List<UUID> chosenPlayerIds,
                                         int insertionIndex) {
        if (remainingChooserIds.isEmpty()) {
            completeSacrifices(entry, chosenCreatureIds, insertionIndex);
            return;
        }

        UUID choosingPlayerId = remainingChooserIds.getFirst();
        List<UUID> remaining = List.copyOf(remainingChooserIds.subList(1, remainingChooserIds.size()));
        List<UUID> validCreatureIds = creatureIds(gameData, choosingPlayerId, controllerId);
        if (validCreatureIds.isEmpty()) {
            continueCreatureChoices(gameData, entry, controllerId, opponentId, remaining,
                    chosenCreatureIds, chosenPlayerIds, insertionIndex);
            return;
        }
        if (validCreatureIds.size() == 1) {
            List<UUID> nextChosenCreatureIds = new ArrayList<>(chosenCreatureIds);
            nextChosenCreatureIds.add(validCreatureIds.getFirst());
            List<UUID> nextChosenPlayerIds = new ArrayList<>(chosenPlayerIds);
            nextChosenPlayerIds.add(choosingPlayerId);
            continueCreatureChoices(gameData, entry, controllerId, opponentId, remaining,
                    nextChosenCreatureIds, nextChosenPlayerIds, insertionIndex);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.InfernalOfferingCreatureChoice(
                controllerId, opponentId, choosingPlayerId, remaining, chosenCreatureIds, chosenPlayerIds,
                entry.getCard().getName()));
        playerInputService.beginPermanentChoice(gameData, choosingPlayerId, validCreatureIds,
                gameData.interaction.permanentChoiceContext(),
                entry.getCard().getName() + " — choose a creature to sacrifice.");
    }

    private void completeSacrifices(StackEntry entry, List<UUID> creatureIds, int insertionIndex) {
        // The sacrifices are the next step, one event, so a CR 616.1 question is asked before either creature leaves
        // (the opponent may have been chosen and creatures picked already), and the draws follow it, read from the
        // departure's results.
        if (creatureIds.isEmpty()) {
            return;
        }
        stepDepartureSupport.departAsStepAt(entry, insertionIndex, StepDepartureSupport.sacrifices(creatureIds),
                List.of(new ContinueAfterDepartureEffect(new InfernalOfferingEffect(true))));
    }

    /**
     * "Each player who sacrificed a creature this way draws two cards": the controller, then the chosen opponent,
     * each draws two if the departure just recorded shows a creature they sacrificed (by its last-known controller,
     * CR 608.2h).
     */
    @Override
    public void continueAfterDeparture(GameData gameData, StackEntry entry, CardEffect effect) {
        Set<UUID> sacrificingPlayerIds = new HashSet<>();
        for (DepartureResult result : entry.getResolutionResults().departures()) {
            if (result.sacrificed() && result.lastKnownControllerId() != null) {
                sacrificingPlayerIds.add(result.lastKnownControllerId());
            }
        }
        for (UUID playerId : List.of(entry.getControllerId(), entry.getTargetId())) {
            if (sacrificingPlayerIds.remove(playerId) && gameData.playerIds.contains(playerId)) {
                drawService.resolveDrawCards(gameData, playerId, 2);
            }
        }
    }

    private void queueGraveyardReturns(GameData gameData, UUID controllerId, UUID opponentId) {
        CardTypePredicate creatureFilter = new CardTypePredicate(CardType.CREATURE);
        gameData.pendingGraveyardReturnQueue.addLast(new PendingGraveyardReturnChoice(
                controllerId, 1, creatureFilter, GraveyardChoiceDestination.BATTLEFIELD,
                false, true, false));
        gameData.pendingGraveyardReturnQueue.addLast(new PendingGraveyardReturnChoice(
                opponentId, 1, creatureFilter, GraveyardChoiceDestination.BATTLEFIELD,
                false, true, false));
        graveyardReturnSupport.beginNextGraveyardReturnFromQueue(gameData);
    }

    private List<UUID> creatureIds(GameData gameData, UUID playerId, UUID sourceControllerId) {
        if (playerId == null || !gameData.playerIds.contains(playerId)
                || !gameQueryService.canEffectCauseSacrifice(gameData, playerId, sourceControllerId)) {
            return List.of();
        }
        return destructionSupport.collectCreatureIds(gameData, playerId,
                permanent -> !gameQueryService.cantBeChosenToSacrifice(gameData, permanent));
    }

    private List<UUID> opponentsOf(GameData gameData, UUID controllerId) {
        return gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(controllerId))
                .toList();
    }
}
