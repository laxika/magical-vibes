package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect;
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
public class ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffectHandler
        implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final InputCompletionService inputCompletionService;
    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ChooseCounterTypeOnControlledCreatureThenPutOnOtherCreaturesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        List<Permanent> candidates = counteredCreatures(gameData, controllerId);
        if (candidates.isEmpty()) {
            return;
        }

        if (candidates.size() == 1) {
            beginCounterChoice(gameData, entry, candidates.getFirst());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(new PermanentChoiceContext.ContractualSafeguardReference());
        playerInputService.beginPermanentChoice(
                gameData,
                controllerId,
                candidates.stream().map(Permanent::getId).toList(),
                "Choose a creature you control with a counter.");
    }

    public void completeReferenceChoice(GameData gameData, UUID permanentId) {
        StackEntry entry = gameData.pendingEffectResolutionEntry;
        if (entry == null) {
            return;
        }

        Permanent chosen = gameQueryService.findPermanentById(gameData, permanentId);
        if (chosen == null
                || !entry.getControllerId().equals(gameQueryService.findPermanentController(gameData, permanentId))
                || !gameQueryService.isCreature(gameData, chosen)
                || !beginCounterChoice(gameData, entry, chosen)) {
            inputCompletionService.sbaProcessMayAbilitiesThenAutoPassPreservingPriority(gameData);
        }
    }

    private boolean beginCounterChoice(GameData gameData, StackEntry entry, Permanent reference) {
        List<CounterType> counterTypes = counterTypesOn(reference);
        if (counterTypes.isEmpty()) {
            return false;
        }

        entry.setChosenPermanentId(reference.getId());
        String sourceName = entry.getCard() == null ? "Contractual Safeguard" : entry.getCard().getName();
        playerInputService.beginAddAnotherCounterTypeChoice(
                gameData,
                entry.getControllerId(),
                reference.getId(),
                sourceName,
                counterTypes,
                false,
                true);
        return true;
    }

    private List<Permanent> counteredCreatures(GameData gameData, UUID controllerId) {
        List<Permanent> candidates = new ArrayList<>();
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(controllerId, List.of())) {
            if (gameQueryService.isCreature(gameData, permanent) && !counterTypesOn(permanent).isEmpty()) {
                candidates.add(permanent);
            }
        }
        return candidates;
    }

    private List<CounterType> counterTypesOn(Permanent permanent) {
        List<CounterType> counterTypes = new ArrayList<>();
        for (CounterType counterType : CounterType.values()) {
            if (counterType != CounterType.ANY && counterType != CounterType.SILVER
                    && permanent.getCounterCount(counterType) > 0) {
                counterTypes.add(counterType);
            }
        }
        return counterTypes;
    }
}
