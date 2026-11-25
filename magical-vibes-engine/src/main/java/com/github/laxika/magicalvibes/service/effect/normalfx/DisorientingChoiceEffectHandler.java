package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DisorientingChoiceState;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DisorientingChoiceEffect;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.service.FaceDownVisibility;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Resolves Disorienting Choice's independent exile decisions and follow-up land search. The chosen permanents'
 * controllers decide in turn order, each knowing the earlier decisions, then the permanents are exiled together
 * (its ruling; CR 101.4).
 */
@Component
@RequiredArgsConstructor
public class DisorientingChoiceEffectHandler implements NormalEffectHandlerBean {

    private final ExileSupport exileSupport;
    private final GameQueryService gameQueryService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final SearchLibraryEffectHandler searchLibraryEffectHandler;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DisorientingChoiceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        DisorientingChoiceState state = gameData.disorientingChoice;
        String sourceName = entry.getCard().getName();

        if (!state.active) {
            state.reset();
            state.active = true;
            state.selectedTargetIds.addAll(entry.targetsForEffect(effect));
            state.remainingTargetIds.addAll(inApnapOrderOfControllers(gameData, state.selectedTargetIds));
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            if (ChoiceContext.DisorientingChoiceExileChoice.EXILE.equals(chosenMode)) {
                state.exileTargetIds.add(state.currentTargetId);
            }
        }

        advance(gameData, entry, sourceName);
    }

    private void advance(GameData gameData, StackEntry entry, String sourceName) {
        DisorientingChoiceState state = gameData.disorientingChoice;
        while (!state.remainingTargetIds.isEmpty()) {
            UUID targetId = state.remainingTargetIds.removeFirst();
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null) {
                continue;
            }

            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            if (controllerId == null) {
                continue;
            }

            state.currentTargetId = targetId;
            gameData.rerunCurrentEffectAfterInteraction = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                    controllerId, null, null,
                    new ChoiceContext.DisorientingChoiceExileChoice(controllerId, targetId, sourceName),
                    List.of(ChoiceContext.DisorientingChoiceExileChoice.EXILE,
                            ChoiceContext.DisorientingChoiceExileChoice.KEEP),
                    sourceName + " - " + FaceDownVisibility.publicName(gameData, target) + " may be exiled."));
            return;
        }

        // Every controller has decided: the permanents they chose leave together (CR 101.4).
        for (UUID targetId : state.exileTargetIds) {
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target != null) {
                exileSupport.exilePermanentAndLog(gameData, target, sourceName);
            }
        }

        int remainingOnBattlefield = (int) state.selectedTargetIds.stream()
                .filter(targetId -> gameQueryService.findPermanentById(gameData, targetId) != null)
                .count();
        state.reset();
        gameData.rerunCurrentEffectAfterInteraction = false;
        if (remainingOnBattlefield == 0) {
            return;
        }

        SearchLibraryEffect search = new SearchLibraryEffect(
                new EventValue(),
                new CardTypePredicate(CardType.LAND),
                LibrarySearchDestination.BATTLEFIELD_TAPPED);
        StackEntry searchEntry = StackEntry.derivedFrom(entry, entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(search), entry.getTargetId(), entry.getSourcePermanentId());
        searchEntry.setEventValue(remainingOnBattlefield);
        searchEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        searchLibraryEffectHandler.resolve(gameData, searchEntry, search);
    }

    /** The targets ordered by their controllers' APNAP order (CR 101.4), each controller's in target order. */
    private List<UUID> inApnapOrderOfControllers(GameData gameData, List<UUID> targetIds) {
        List<UUID> players = new java.util.ArrayList<>(gameData.orderedPlayerIds);
        int activeIndex = players.indexOf(gameData.activePlayerId);
        if (activeIndex > 0) {
            java.util.Collections.rotate(players, -activeIndex);
        }
        java.util.Map<UUID, Integer> order = new java.util.HashMap<>();
        for (UUID targetId : targetIds) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
            int index = controllerId == null ? -1 : players.indexOf(controllerId);
            order.put(targetId, index < 0 ? players.size() : index);
        }
        return targetIds.stream().sorted(java.util.Comparator.comparingInt(order::get)).toList();
    }
}
