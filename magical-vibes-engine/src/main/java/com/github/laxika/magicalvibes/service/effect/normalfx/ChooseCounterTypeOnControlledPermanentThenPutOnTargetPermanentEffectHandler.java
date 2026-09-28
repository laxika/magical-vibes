package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.InputCompletionService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCounterTypeOnControlledPermanentThenPutOnTargetPermanentEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        if (targetId == null) {
            return;
        }

        List<Permanent> candidates = counteredPermanents(gameData, entry.getControllerId());
        if (candidates.isEmpty()) {
            return;
        }

        if (candidates.size() == 1) {
            beginCounterChoice(gameData, entry, candidates.getFirst(), targetId);
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseCounterTypeOnControlledPermanentReference());
        playerInputService.beginPermanentChoice(
                gameData,
                entry.getControllerId(),
                candidates.stream().map(Permanent::getId).toList(),
                "Choose a permanent you control with a counter.");
    }

    public void completeReferenceChoice(GameData gameData, UUID permanentId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        beginCounterChoice(gameData, entry, chosen, entry.getTargetId());
    }

    private void beginCounterChoice(GameData gameData, StackEntry entry, Permanent reference, UUID targetId) {
        List<com.github.laxika.magicalvibes.model.CounterType> counterTypes =
                RemoveChosenCountersFromTargetPermanentEffectHandler.counterTypesOn(reference);
        if (counterTypes.isEmpty() || targetId == null) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
            return;
        }

        playerInputService.beginAddAnotherCounterTypeChoice(
                gameData,
                entry.getControllerId(),
                reference.getId(),
                entry.getCard().getName(),
                counterTypes,
                false,
                false,
                targetId);
    }

    private List<Permanent> counteredPermanents(GameData gameData, UUID controllerId) {
        List<Permanent> candidates = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (!RemoveChosenCountersFromTargetPermanentEffectHandler.counterTypesOn(permanent).isEmpty()) {
                candidates.add(permanent);
            }
        }
        return candidates;
    }
}
