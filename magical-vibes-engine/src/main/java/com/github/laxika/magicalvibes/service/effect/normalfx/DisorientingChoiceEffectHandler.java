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
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Resolves Disorienting Choice's independent exile decisions and follow-up land search. */
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
            state.remainingTargetIds.addAll(state.selectedTargetIds);
        }

        if (state.chosenMode != null) {
            String chosenMode = state.chosenMode;
            state.chosenMode = null;
            if (ChoiceContext.DisorientingChoiceExileChoice.EXILE.equals(chosenMode)) {
                Permanent target = gameQueryService.findPermanentById(gameData, state.currentTargetId);
                if (target != null) {
                    exileSupport.exilePermanentAndLog(gameData, target, sourceName);
                }
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
                    sourceName + " - " + target.getCard().getName() + " may be exiled."));
            return;
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
        StackEntry searchEntry = new StackEntry(entry.getEntryType(), entry.getCard(), entry.getControllerId(),
                entry.getDescription(), List.of(search), entry.getTargetId(), entry.getSourcePermanentId());
        searchEntry.setEventValue(remainingOnBattlefield);
        searchEntry.setSourcePermanentSnapshot(entry.getSourcePermanentSnapshot());
        searchLibraryEffectHandler.resolve(gameData, searchEntry, search);
    }
}
