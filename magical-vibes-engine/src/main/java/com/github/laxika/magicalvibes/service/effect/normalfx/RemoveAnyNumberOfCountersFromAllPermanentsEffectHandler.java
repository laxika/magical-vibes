package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveAnyNumberOfCountersFromAllPermanentsEffect;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RemoveAnyNumberOfCountersFromAllPermanentsEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInputService playerInputService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return RemoveAnyNumberOfCountersFromAllPermanentsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        entry.setEventValue(0);
        Map<String, ChoiceContext.CounterSelection> counterOptions = counterOptions(gameData);
        if (!counterOptions.isEmpty()) {
            beginChoice(gameData, entry, counterOptions);
        }
    }

    public void beginChoice(GameData gameData, StackEntry entry,
                            Map<String, ChoiceContext.CounterSelection> counterOptions) {
        playerInputService.beginRemoveAnyNumberOfCountersFromAllPermanentsChoice(
                gameData, entry, counterOptions);
    }

    public Map<String, ChoiceContext.CounterSelection> counterOptions(GameData gameData) {
        List<Permanent> eligiblePermanents = new ArrayList<>();
        List<UUID> controllerIds = new ArrayList<>(gameData.playerBattlefields.keySet());
        controllerIds.sort(Comparator.comparing(UUID::toString));
        for (UUID controllerId : controllerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield != null) {
                for (Permanent permanent : battlefield) {
                    if (!RemoveChosenCountersFromTargetPermanentEffectHandler.counterTypesOn(permanent).isEmpty()) {
                        eligiblePermanents.add(permanent);
                    }
                }
            }
        }

        Map<String, Integer> totalsByLabel = new HashMap<>();
        for (Permanent permanent : eligiblePermanents) {
            for (CounterType counterType : removableCounterTypes(permanent)) {
                totalsByLabel.merge(optionLabel(permanent, counterType), 1, Integer::sum);
            }
        }

        Map<String, Integer> occurrencesByLabel = new HashMap<>();
        Map<String, ChoiceContext.CounterSelection> options = new LinkedHashMap<>();
        for (Permanent permanent : eligiblePermanents) {
            for (CounterType counterType : removableCounterTypes(permanent)) {
                String baseLabel = optionLabel(permanent, counterType);
                int occurrence = occurrencesByLabel.merge(baseLabel, 1, Integer::sum);
                String label = totalsByLabel.get(baseLabel) == 1
                        ? baseLabel
                        : baseLabel + " (" + occurrence + ")";
                if (options.containsKey(label)) {
                    label = baseLabel + " (" + occurrence + " - "
                            + permanent.getId().toString().substring(0, 8) + ")";
                }
                options.put(label, new ChoiceContext.CounterSelection(permanent.getId(), counterType));
            }
        }
        return options;
    }

    private static List<CounterType> removableCounterTypes(Permanent permanent) {
        return RemoveChosenCountersFromTargetPermanentEffectHandler.counterTypesOn(permanent);
    }

    private static String optionLabel(Permanent permanent, CounterType counterType) {
        return permanent.getCard().getName() + " — "
                + ChoiceContext.RemoveChosenCountersChoice.counterLabel(counterType);
    }
}
