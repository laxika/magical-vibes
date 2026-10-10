package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.VillainousChoiceState;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerChoosesOneEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Opens a modal choice for the player identified by the stack entry's target. A villainous choice
 * splices the chosen option's effects after itself and, when a replacement effect (The Valeyard)
 * owes additional repetitions, re-queues itself behind them so each choice is fully performed one
 * at a time.
 */
@Component
@RequiredArgsConstructor
public class TargetPlayerChoosesOneEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;
    private final VillainousChoiceSupport villainousChoiceSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return TargetPlayerChoosesOneEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID chooserId = entry.getTargetId() != null ? entry.getTargetId() : entry.getControllerId();
        TargetPlayerChoosesOneEffect targetChoice = (TargetPlayerChoosesOneEffect) effect;
        if (targetChoice.villainousChoice()) {
            resolveVillainousChoice(gameData, entry, targetChoice, chooserId);
            return;
        }
        playerInputService.beginChooseModeChoice(gameData, chooserId, entry.getCard(),
                new ChooseOneEffect(targetChoice.options()));
    }

    private void resolveVillainousChoice(GameData gameData, StackEntry entry,
                                         TargetPlayerChoosesOneEffect effect, UUID chooserId) {
        VillainousChoiceState state = gameData.villainousChoice;
        if (state.chosenMode != null) {
            String chosen = state.chosenMode;
            state.chosenMode = null;
            List<CardEffect> followUp = new ArrayList<>(effect.options().stream()
                    .filter(option -> option.label().equals(chosen))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("Invalid villainous choice: " + chosen))
                    .effects());
            if (state.additionalChoicesRemaining > 0) {
                state.additionalChoicesRemaining--;
                followUp.add(effect);
            } else {
                state.reset();
            }
            entry.insertEffectsToResolve(entry.getResolvingEffectIndex() + 1, followUp);
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }

        if (!gameData.playerIds.contains(chooserId)) {
            state.reset();
            gameData.rerunCurrentEffectAfterInteraction = false;
            return;
        }
        if (!state.active) {
            state.reset();
            state.active = true;
        }
        gameData.rerunCurrentEffectAfterInteraction = true;
        List<String> labels = effect.options().stream().map(ChooseOneEffect.ChooseOneOption::label).toList();
        villainousChoiceSupport.beginChoice(gameData, chooserId, entry.getCard().getName(), labels.getFirst(),
                labels, entry.getCard().getName() + " — Choose a villainous choice.");
    }
}
